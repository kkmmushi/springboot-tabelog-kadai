package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.ShopService;

@Controller
public class ReservationController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationService reservationService;

    // 予約確認ページを表示
    @PostMapping("/reservation-confirmation")
    public String showReservationConfirmation(
            @RequestParam("shopId") Integer shopId,
            @RequestParam("reservationDate") LocalDate reservationDate,
            @RequestParam("reservationTime") LocalTime reservationTime,
            @RequestParam("reservationPersons") Integer reservationPersons,
            Model model) {

        // 店舗情報を取得
        Shop shop = shopService.findShopById(shopId);
        

        // 予約内容を作成
        Reservation reservation = new Reservation();
        reservation.setShopId(shop);
        reservation.setReservationDay(reservationDate);
        reservation.setReservationTime(reservationTime);
        reservation.setReservationPersons(reservationPersons);

        // モデルに店舗情報と予約情報をセット
        model.addAttribute("shop", shop);
        model.addAttribute("reservation", reservation);

        // 予約確認ページを表示
        return "reservation-confirm"; // reservation-confirmation.html
    }

    // 予約内容を保存して完了ページに遷移
 // 予約内容を保存して完了ページに遷移
    @PostMapping("/confirm-reservation")
    public String saveReservation(@RequestParam("shopId") Integer shopId,
                                  @RequestParam("reservationDate") LocalDate reservationDay,
                                  @RequestParam("reservationTime") LocalTime reservationTime,
                                  @RequestParam("reservationPersons") Integer reservationPersons,
                                  @AuthenticationPrincipal UserDetails userDetails,  // Spring Securityでユーザー情報を取得
                                  Model model) {

        // shopId を使って Shop エンティティを取得
        Shop shop = shopService.findShopById(shopId);

        // ユーザー情報を取得 (Spring Securityから取得)
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email);

        // 予約情報を作成
        Reservation reservation = new Reservation();
        reservation.setShopId(shop);  // 予約した店を設定
        reservation.setUserId(user);  // 予約したユーザーを設定
        reservation.setReservationDay(reservationDay);  // 予約日を設定
        reservation.setReservationTime(reservationTime);  // 予約時間を設定
        reservation.setReservationPersons(reservationPersons);  // 予約人数を設定

        // 予約を保存
        reservationService.saveReservation(reservation);

        // 保存した予約情報をモデルにセット
        model.addAttribute("reservation", reservation);
        model.addAttribute("shop", shop); 

        // 予約成功ページを表示
        return "reservation-success"; // reservation-success.html
    }

    // 店舗詳細ページに戻るためのGETメソッド
    @GetMapping("/cancel/{shopId}")
    public String cancelReservation(@PathVariable("shopId") Long shopId) {
        // 店舗詳細ページへ戻る
        return "redirect:/shop-details/{shopId}";
    }
    
    @GetMapping("/reservations")
    public String getUserReservations(Model model) {
        // SecurityContextから認証情報を取得
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 認証情報がnullでないことを確認
        if (authentication != null && authentication.isAuthenticated()) {
            // 認証されたユーザー情報を取得
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // UserDetailsImplからUserエンティティを取得
            User user = userDetails.getUser();

            // ユーザーに基づいて予約情報を取得
            List<Reservation> reservations = reservationService.findByUser(user);

            // 予約内容をモデルに追加
            model.addAttribute("reservations", reservations);

            // ログ出力（デバッグ用）
            System.out.println("予約ユーザー：" + user);
            System.out.println("予約内容" + reservations);
        } else {
            // 認証情報がない場合の処理（例えばエラーメッセージを表示）
            System.out.println("認証情報がありません。ログインが必要です。");
        }

        return "reservation-list"; // Thymeleaf テンプレートを返す
    }
    
    // 予約を取り消すメソッド
    @GetMapping("/reservations/cancel/{no}")
    public String cancelReservation(@PathVariable Long no, RedirectAttributes redirectAttributes) {
        boolean isDeleted = reservationService.cancelReservation(no);
        
        if (isDeleted) {
            // 予約が正常に削除された場合、メッセージを追加
            redirectAttributes.addFlashAttribute("message", "予約を削除しました");
        } else {
            // 予約削除に失敗した場合、エラーメッセージを追加
            redirectAttributes.addFlashAttribute("message", "予約の削除に失敗しました");
        }
        
        // 予約一覧ページにリダイレクト
        return "redirect:/reservations";
    }
    
}
