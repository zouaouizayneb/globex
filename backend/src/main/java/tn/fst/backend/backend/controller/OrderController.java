package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.OrderStatus;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.OrderService;

import java.util.List;

/**
 * Controller pour la gestion des commandes
 * Endpoints: historique, suivi, détails, annulation, retours, remboursements
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Obtenir l'historique des commandes du client
     *
     * GET /api/orders/history
     *
     * Retourne toutes les commandes du client triées par date (plus récentes en premier)
     */
    @GetMapping("/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        List<OrderHistoryResponse> orders = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Obtenir les détails complets d'une commande
     *
     * GET /api/orders/{orderId}
     *
     * Retourne tous les détails: items, montants, paiement, livraison
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsResponse> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderDetailsResponse details = orderService.getOrderDetails(userId, orderId);
        return ResponseEntity.ok(details);
    }

    /**
     * Suivre une commande (tracking)
     *
     * GET /api/orders/{orderId}/track
     *
     * Retourne le statut de la commande, paiement, livraison et numéro de suivi
     */
    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderTrackingResponse> trackOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderTrackingResponse tracking = orderService.trackOrder(userId, orderId);
        return ResponseEntity.ok(tracking);
    }

    /**
     * Obtenir les commandes par statut
     * <p>
     * GET /api/orders/status/PENDING
     * GET /api/orders/status/DELIVERED
     * <p>
     * Filtrer les commandes par statut spécifique
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @PathVariable String status,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = orderService.getOrdersByStatus(userId, orderStatus);
        return ResponseEntity.ok(orders);
    }

    /**
     * Annuler une commande
     *
     * POST /api/orders/{orderId}/cancel
     *
     * Body:
     * {
     *   "reason": "Je ne veux plus ce produit"
     * }
     *
     * Annule la commande si possible et initie le remboursement si payée
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderCancellationResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancellationRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderCancellationResponse response = orderService.cancelOrder(
                userId,
                orderId,
                request.getReason()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Demander un retour de commande
     *
     * POST /api/orders/{orderId}/return
     *
     * Body:
     * {
     *   "reason": "Produit défectueux",
     *   "description": "Le produit est arrivé cassé",
     *   "itemIds": [1, 2]
     * }
     *
     * Crée une demande de retour pour une commande livrée
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ReturnRequestResponse> requestReturn(
            @PathVariable Long orderId,
            @Valid @RequestBody ReturnRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        ReturnRequestResponse response = orderService.requestReturn(userId, orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Traiter un remboursement (ADMIN uniquement - à sécuriser)
     *
     * POST /api/orders/{orderId}/refund
     *
     * Body:
     * {
     *   "reason": "Retour accepté"
     * }
     *
     * Traite le remboursement d'une commande
     * TODO: Ajouter autorisation ADMIN
     */
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<RefundResponse> processRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody RefundRequest request,
            Authentication authentication) {

        // TODO: Vérifier que l'utilisateur est ADMIN
        RefundResponse response = orderService.processRefund(orderId, request.getReason());
        return ResponseEntity.ok(response);
    }

    // Helper method
    private Long getCurrentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }
}