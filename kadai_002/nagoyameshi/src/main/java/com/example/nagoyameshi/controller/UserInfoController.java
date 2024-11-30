package com.example.nagoyameshi.controller;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.PassmissingEventPublisher;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsServiceImpl;
import com.example.nagoyameshi.service.EmailService;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;


@Controller
public class UserInfoController{
	
	private final UserDetailsServiceImpl userDetailsService ;
	private final UserService userService;
	private final UserRepository userRepository;
	private final SignupForm signupForm;
	private final SignupEventPublisher signupEventPublisher;
	private final PassmissingEventPublisher passmissingEventPublisher;
	private final VerificationTokenService verificationTokenService;
	private final EmailService emailService;
	private final Map<Integer, String> emailChangeRequests = new ConcurrentHashMap<>();
	private HttpServletRequest request;
	private final PasswordEncoder passwordEncoder;
	
	
	@Autowired
	public UserInfoController(UserDetailsServiceImpl userDetailsService, UserService userService, SignupForm signupForm,SignupEventPublisher signupEventPublisher,PassmissingEventPublisher passmissingEventPublisher,VerificationTokenService verificationTokenService,UserRepository userRepository,EmailService emailService,HttpServletRequest request,PasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.userService = userService;
		this.signupForm = signupForm;
		this.userRepository = userRepository;
		this.signupEventPublisher = signupEventPublisher;
		this.passmissingEventPublisher = passmissingEventPublisher; 
	    this.verificationTokenService =verificationTokenService;
	    this.emailService = emailService;
	    this.request = request;
	    this.passwordEncoder = passwordEncoder;
		
	}
	
	
	@GetMapping("/userInfo")
	public String viewProfile(Model model, HttpSession session) {
	    User user = userService.getCurrentUser();  // 現在のユーザーを取得
	    model.addAttribute("user", user);

	    // セッションからメッセージを取得して表示
	    String successMessage = (String) session.getAttribute("successMessage");
	    String errorMessage = (String) session.getAttribute("errorMessage");
	    
	    // もし"usedsuccessmessage"がセッションにあれば、それをモデルに追加し、usedsuccessmessageは削除
	    String usedSuccessMessage = (String) session.getAttribute("usedsuccessmessage");
	    if (usedSuccessMessage != null) {
	        model.addAttribute("successMessage", usedSuccessMessage);  // usedsuccessmessageをmodelに追加
	        session.removeAttribute("usedsuccessmessage");  // usedsuccessmessageを削除
	    }
	    
	    // もし"successMessage"がセッションにある場合、それを"usedsuccessmessage"としてセッションに保存
	    if (successMessage != null) {
	        session.setAttribute("usedsuccessmessage", successMessage);  // successMessageをusedsuccessmessageとして保存
	        session.removeAttribute("successMessage");  // successMessageをセッションから削除
	    }

	    
	    // もし"usederrorMessage"がセッションにあれば、それをモデルに追加し、usederrorMessageは削除
	    String usedErrorMessage = (String) session.getAttribute("usederrorMessage");
	    if (usedErrorMessage != null) {
	        model.addAttribute("errorMessage", usedErrorMessage);  // usederrorMessageをmodelに追加
	        session.removeAttribute("usederrorMessage");  // usederrorMessageを削除
	    }


	    // 同様に、"errorMessage"の処理を追加
	    if (errorMessage != null) {
	        session.setAttribute("usederrorMessage", errorMessage);  // errorMessageをusederrorMessageとして保存
	        session.removeAttribute("errorMessage");  // errorMessageをセッションから削除
	    }



	    return "user/userInfo";  // メッセージを表示
	}



	
	// プロフィール編集ページ
    @GetMapping("/editProfile")
    public String editProfile(Model model) {
        User user = userService.getCurrentUser(); // 現在のユーザー情報を取得
        model.addAttribute("user", user);
        return "user/editProfile"; // editProfile.html へ渡す
    }

 // プロフィール更新処理
    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam String name, @RequestParam String phoneNumber, RedirectAttributes redirectAttributes) {
        // 現在のユーザー情報を取得
        User user = userService.getCurrentUser();

        // ユーザー情報を更新する
        userService.updateUser(user, name, phoneNumber); 

        // 更新成功メッセージをセット
        redirectAttributes.addFlashAttribute("successMessage", "プロフィールが更新されました！");
        
        // ユーザー情報ページにリダイレクト
        return "redirect:/userInfo";
    }


    // メールアドレス変更ページ
    @GetMapping("/editEmail")
    public String editEmail() {
        return "user/editEmail"; // editEmail.html へ渡す
    }

    // パスワード変更ページ
    @GetMapping("/editPassword")
    public String editPassword() {
        return "user/editPassword"; // editPassword.html へ渡す
    }
    
    //--------------------------これ以降はメール変更用
    // メールアドレス確認用の確認メールを送信
    @PostMapping("/sendEmailVerification")
    public String sendVerificationEmail(@RequestParam("email") String email, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = userService.getCurrentUser(); // 現在の認証されたユーザーを取得

        // 一時的なマップにユーザーIDと新しいメールアドレスを保存
        emailChangeRequests.put(user.getId(), email);

        // ユーザーに認証トークンを生成
        String token = verificationTokenService.createAndReturnToken(user);

        // トークンを含む認証メールを送信
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String verificationUrl = baseUrl + "/user/verifyEmail?token=" + token;
        String emailText = "以下のリンクをクリックして、メールアドレスを確認してください: " + verificationUrl;

        // メール送信サービスを呼び出し
        emailService.sendEmail(email, "メールアドレス確認", emailText);

        // メール送信完了メッセージを表示
        redirectAttributes.addFlashAttribute("successMessage", "確認メールが送信されました。メール内のリンクをクリックして認証してください。");

        return "redirect:/editEmail"; // フォームにリダイレクト
    }
  
    
    // メールアドレスの確認処理
    @GetMapping("/user/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes, Model model) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);

        if (verificationToken != null) {
            User user = verificationToken.getUser();
            Integer userId = user.getId();

            // 新しいメールアドレスを取得
            String newEmail = emailChangeRequests.get(userId);

            if (newEmail != null) {
                // メールアドレスを更新
                user.setEmail(newEmail);
                userRepository.save(user);

                // 一時的なリストから変更情報を削除
                emailChangeRequests.remove(userId);

                // ユーザー情報をモデルに追加
                model.addAttribute("user", user);

                // 成功メッセージをフラッシュスコープに追加
                redirectAttributes.addFlashAttribute("successMessage", "メールアドレスが正常に確認されました！");

                return "redirect:/userInfo";  // メールアドレス更新後、ユーザー情報ページにリダイレクト
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "メールアドレスの変更情報が見つかりませんでした。");
                return "redirect:/editEmail";  // 変更情報が見つからない場合、メール編集ページにリダイレクト
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "無効な認証トークンです。");
            return "redirect:/editEmail";  // トークンが無効な場合、メール編集ページにリダイレクト
        }
    }
    
  
    //------------------------パスワード変更用-----------------
    
    @PostMapping("/updatePassword")
    public String updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        
        // ログイン中のユーザーを取得
    	  String email = principal.getName();  // メールアドレスを取得
          User user = userRepository.findByEmail(email);  // メールアドレスでユーザーを検索

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません。");
            return "redirect:/updatePassword";  // エラーメッセージを持って再度同じページへ
        }

        // 1. 現在のパスワードが正しいか確認
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "現在のパスワードが正しくありません。");
            return "redirect:/updatePassword";  // エラーメッセージを持って再度同じページへ
        }

        // 2. 新しいパスワードと確認用パスワードが一致するか確認
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "新しいパスワードと確認用パスワードが一致しません。");
            return "redirect:/updatePassword";  // エラーメッセージを持って再度同じページへ
        }

        // 3. 新しいパスワードのハッシュ化
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);

        // 4. ユーザーのパスワードを更新
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "パスワードが正常に更新されました。");
        return "redirect:/userInfo";  // パスワード更新後のリダイレクト先（例：ユーザー情報ページ）
    }
    
}