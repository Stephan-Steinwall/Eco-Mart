package com.cybrixx.ecomartbackend;

import com.cybrixx.ecomartbackend.controller.AdminOfferController;
import com.cybrixx.ecomartbackend.dto.OfferRequestDTO;
import com.cybrixx.ecomartbackend.dto.OfferResponseDTO;
import com.cybrixx.ecomartbackend.entity.Offer;
import com.cybrixx.ecomartbackend.entity.Order;
import com.cybrixx.ecomartbackend.entity.User;
import com.cybrixx.ecomartbackend.entity.enums.OrderStatus;
import com.cybrixx.ecomartbackend.repository.OfferRepository;
import com.cybrixx.ecomartbackend.repository.OrderRepository;
import com.cybrixx.ecomartbackend.repository.ProductRepository;
import com.cybrixx.ecomartbackend.repository.UserRepository;
import com.cybrixx.ecomartbackend.service.FcmService;
import com.cybrixx.ecomartbackend.service.OrderNotificationListener;
import com.cybrixx.ecomartbackend.service.OrderService;
import com.cybrixx.ecomartbackend.service.OrderStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Plain unit tests (no Spring context, no database, no Firebase). */
class PushNotificationTests {

    // ---------- Offers ----------

    private OfferRequestDTO validOffer() {
        OfferRequestDTO r = new OfferRequestDTO();
        r.setTitle("Weekend Fresh Sale");
        r.setMessage("Fresh produce, fresh prices.");
        r.setDiscountPercent(20);
        r.setDurationMinutes(180);
        return r;
    }

    private OfferRepository offerRepoThatSaves() {
        OfferRepository repo = mock(OfferRepository.class);
        when(repo.save(any(Offer.class))).thenAnswer(inv -> {
            Offer o = inv.getArgument(0);
            o.setId(7L);
            return o;
        });
        return repo;
    }

    @Test
    void offerWithInvalidFieldsIsRejectedAndNothingIsSavedOrSent() {
        OfferRepository repo = offerRepoThatSaves();
        FcmService fcm = mock(FcmService.class);
        AdminOfferController controller = new AdminOfferController(repo, fcm);

        OfferRequestDTO noTitle = validOffer();
        noTitle.setTitle("  ");
        OfferRequestDTO hugeDiscount = validOffer();
        hugeDiscount.setDiscountPercent(95);
        OfferRequestDTO zeroDuration = validOffer();
        zeroDuration.setDurationMinutes(0);

        for (OfferRequestDTO bad : new OfferRequestDTO[]{noTitle, hugeDiscount, zeroDuration}) {
            ResponseEntity<?> response = controller.createOffer(bad);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        verifyNoInteractions(fcm);
        verify(repo, never()).save(any());
    }

    @Test
    void confirmedOfferIsSavedAndBroadcastToTheOffersTopic() {
        OfferRepository repo = offerRepoThatSaves();
        FcmService fcm = mock(FcmService.class);
        when(fcm.sendToTopic(eq(FcmService.OFFERS_TOPIC), anyMap(), any(Duration.class)))
                .thenReturn(FcmService.PushResult.SENT);
        AdminOfferController controller = new AdminOfferController(repo, fcm);

        ResponseEntity<?> response = controller.createOffer(validOffer());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OfferResponseDTO body = (OfferResponseDTO) response.getBody();
        assertNotNull(body);
        assertEquals("SENT", body.getPushStatus());
        assertTrue(body.isActive());
        // 180 minutes, give or take the test's own runtime
        assertTrue(Math.abs(body.getSecondsRemaining() - 180 * 60) < 5);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(fcm).sendToTopic(eq("offers"), data.capture(), ttl.capture());
        assertEquals("OFFER", data.getValue().get("type"));
        assertEquals("7", data.getValue().get("offerId"));
        assertTrue(data.getValue().get("title").startsWith("20% OFF"));
        assertEquals(Duration.ofMinutes(180), ttl.getValue()); // stale offers aren't delivered after they expire
    }

    @Test
    void offerIsStillSavedWhenFirebaseIsNotConfigured() {
        OfferRepository repo = offerRepoThatSaves();
        // A real FcmService with no key: init() is never called, so it is disabled.
        AdminOfferController controller = new AdminOfferController(repo, new FcmService(""));

        ResponseEntity<?> response = controller.createOffer(validOffer());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("DISABLED", ((OfferResponseDTO) response.getBody()).getPushStatus());
        verify(repo).save(any(Offer.class));
    }

    @Test
    void endingAnOfferExpiresItImmediately() {
        OfferRepository repo = mock(OfferRepository.class);
        Offer live = Offer.builder().id(3L).title("t").message("m").discountPercent(10)
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(Duration.ofHours(2))).build();
        when(repo.findById(3L)).thenReturn(Optional.of(live));
        when(repo.save(any(Offer.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = new AdminOfferController(repo, mock(FcmService.class)).endOffer(3L);

        OfferResponseDTO body = (OfferResponseDTO) response.getBody();
        assertNotNull(body);
        assertFalse(body.isActive());
        assertEquals(0, body.getSecondsRemaining());
    }

    // ---------- Order status -> push ----------

    private OrderService orderServiceWith(Order order, ApplicationEventPublisher publisher) {
        OrderRepository orders = mock(OrderRepository.class);
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        return new OrderService(orders, mock(ProductRepository.class), mock(UserRepository.class), publisher);
    }

    private Order orderOf(OrderStatus status) {
        User customer = User.builder().id(5L).email("c@example.com").password("x").fcmToken("device-token").build();
        return Order.builder().id(42L).user(customer).status(status).latitude(6.9).longitude(79.8).build();
    }

    @Test
    void movingAnOrderToOnTheWayPublishesAnEventForTheCustomersDevice() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        OrderService service = orderServiceWith(orderOf(OrderStatus.PREPARING), publisher);

        service.updateOrderStatus(42L, "ON_THE_WAY");

        ArgumentCaptor<OrderStatusChangedEvent> event = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(publisher).publishEvent(event.capture());
        assertEquals(42L, event.getValue().orderId());
        assertEquals("device-token", event.getValue().fcmToken());
        assertEquals(OrderStatus.ON_THE_WAY, event.getValue().status());
    }

    @Test
    void reselectingTheSameStatusDoesNotNotifyAgain() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        OrderService service = orderServiceWith(orderOf(OrderStatus.ON_THE_WAY), publisher);

        service.updateOrderStatus(42L, "on_the_way");

        verifyNoInteractions(publisher);
    }

    @Test
    void listenerSendsAnOrderStatusPushWithTheDeepLinkData() {
        FcmService fcm = mock(FcmService.class);
        when(fcm.sendToToken(any(), anyMap(), any(Duration.class))).thenReturn(FcmService.PushResult.SENT);
        OrderNotificationListener listener = new OrderNotificationListener(fcm, mock(UserRepository.class));

        listener.onOrderStatusChanged(new OrderStatusChangedEvent(42L, 5L, "device-token", OrderStatus.ON_THE_WAY, 6.9, 79.8));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(fcm).sendToToken(eq("device-token"), data.capture(), any(Duration.class));
        assertEquals("ORDER_STATUS", data.getValue().get("type"));
        assertEquals("42", data.getValue().get("orderId"));
        assertEquals("ON_THE_WAY", data.getValue().get("status"));
        assertEquals("6.9", data.getValue().get("latitude"));
        assertTrue(data.getValue().get("title").contains("on the way"));
        assertTrue(data.getValue().get("body").contains("#42"));
    }

    @Test
    void listenerSkipsCustomersWithoutADeviceAndInitialStatus() {
        FcmService fcm = mock(FcmService.class);
        OrderNotificationListener listener = new OrderNotificationListener(fcm, mock(UserRepository.class));

        listener.onOrderStatusChanged(new OrderStatusChangedEvent(1L, 5L, null, OrderStatus.ON_THE_WAY, null, null));
        listener.onOrderStatusChanged(new OrderStatusChangedEvent(1L, 5L, "t", OrderStatus.PENDING, null, null));

        verifyNoInteractions(fcm);
    }

    @Test
    void listenerForgetsATokenThatFirebaseRejects() {
        FcmService fcm = mock(FcmService.class);
        when(fcm.sendToToken(any(), anyMap(), any(Duration.class))).thenReturn(FcmService.PushResult.INVALID_TOKEN);
        UserRepository users = mock(UserRepository.class);

        new OrderNotificationListener(fcm, users)
                .onOrderStatusChanged(new OrderStatusChangedEvent(1L, 5L, "stale", OrderStatus.DELIVERED, null, null));

        verify(users).clearToken(5L);
    }
}
