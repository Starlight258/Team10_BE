package bdbe.bdbd.pay;

import bdbe.bdbd._core.errors.security.CustomUserDetails;
import bdbe.bdbd.carwash.Carwash;
import bdbe.bdbd.carwash.CarwashService;
import bdbe.bdbd.reservation.ReservationRequest;
import bdbe.bdbd.reservation.ReservationResponse;
import bdbe.bdbd.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;
    private final ReservationService reservationService;
    private final CarwashService carwashService;

    @PostMapping("/ready/{carwash_id}")
    public ResponseEntity<String> requestPaymentReady(
            @PathVariable("carwash_id") Long carwashId,
            @RequestBody PayRequest.PaymentReadyRequest paymentReadyRequest) {
        return payService.requestPaymentReady(
                paymentReadyRequest.getRequestDto(),
                paymentReadyRequest.getSaveDTO(),
                carwashId
        );
    }

    @PostMapping("/approve/{carwash_id}/{bay_id}")
    public ResponseEntity<ReservationResponse.findLatestOneResponseDTO> requestPaymentApproval(
            @PathVariable("carwash_id") Long carwashId,
            @PathVariable("bay_id") Long bayId,
            @RequestBody PayRequest.PaymentApprovalRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return payService.requestPaymentApproval(
                requestDTO.getPayApprovalRequestDTO(),
                carwashId,
                bayId,
                userDetails.getMember(),
                requestDTO.getSaveDTO()
        );
    }
}