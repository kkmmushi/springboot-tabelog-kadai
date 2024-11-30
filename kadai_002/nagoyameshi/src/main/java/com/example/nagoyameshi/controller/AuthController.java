package com.example.nagoyameshi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.PassmissingEventPublisher;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.LoginForm;
import com.example.nagoyameshi.form.PassMissingForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.security.UserDetailsServiceImpl;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;


@Controller
public class AuthController {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SignupForm signupForm;
    private final SignupEventPublisher signupEventPublisher;
    private final PassmissingEventPublisher passmissingEventPublisher;
    private final VerificationTokenService verificationTokenService;

    @Autowired
    public AuthController(UserDetailsServiceImpl userDetailsService, UserService userService, SignupForm signupForm, SignupEventPublisher signupEventPublisher, PassmissingEventPublisher passmissingEventPublisher, VerificationTokenService verificationTokenService, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.signupForm = signupForm;
        this.userRepository = userRepository;
        this.signupEventPublisher = signupEventPublisher;
        this.passmissingEventPublisher = passmissingEventPublisher;
        this.verificationTokenService = verificationTokenService;
    }

    // ユーザー情報を取得して渡す
    private void addUserToModel(HttpSession session, Model model) {
        Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext != null) {
        	
        	UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	User user = userDetails.getUser();

            model.addAttribute("user", user);  // ユーザー情報をmodelに追加
        }
    }

    //----------------------------------------ログイン関連-------------------------------------------

    @GetMapping("/session")
    public String getSessionInfo(HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す
        return "auth/login"; // セッション情報を表示するテンプレート
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid LoginForm loginForm, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す

            // 入力チェックのエラーハンドリング
            if (result.hasErrors()) {
                return "auth/login";
            }

            // 認証処理
            boolean isAuthenticated = userDetailsService.authenticate(loginForm.getEmail(), loginForm.getPassword());
            if (!isAuthenticated) {
                model.addAttribute("error", "メールまたはパスワードが正しくありません");
                return "auth/login";
            }

            // ユーザー情報の取得
            User user = userService.getUserByEmail(loginForm.getEmail());
            model.addAttribute("user", user); // ユーザー情報を渡す

            // 認証情報の準備
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginForm.getEmail());  // ここで UserDetails を取得
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, 
                null,  // パスワードはすでに認証済みなので、nullで問題ない
                userDetails.getAuthorities()  // ユーザーの権限（Roleなど）をセット
            );

            // SecurityContext に認証情報を設定
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // リダイレクトとフラッシュメッセージの設定
            redirectAttributes.addFlashAttribute("successMessage", "ログインしました");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "システムエラーが発生しました");
            return "auth/login";
        }
    }
    //----------------------------------------会員登録関連-----------------------------------------------

    @GetMapping("/signup")
    public String showsignupForm(HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignupForm signupForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest, HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す

        if (userService.isEmailRegistered(signupForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);
            return "auth/signup";
        }

        if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
            return "auth/signup";
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        User createdUser = userService.create(signupForm);
        String requestUrl = httpServletRequest.getRequestURL().toString();
        signupEventPublisher.publishSignupEvent(createdUser, requestUrl);

        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");

        return "redirect:/";
    }

    @GetMapping("/auth/verify")
    public String verifyPage(@ModelAttribute("successMessage") String successMessage, HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す
        model.addAttribute("successMessage", successMessage);
        return "auth/verify";
    }

    @GetMapping("/signup/verify")
    public String verify(@RequestParam(name = "token") String token, HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す

        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);

        if (verificationToken != null) {
            User user = verificationToken.getUser();
            userService.enableUser(user);
            model.addAttribute("successMessage", "会員登録が完了しました。");
        } else {
            model.addAttribute("errorMessage", "トークンが無効です。");
        }

        return "auth/verify";
    }

    //--------------------------------------パスワードを忘れた場合------------------------------------

    @GetMapping("/passmissing")
    public String showPassMissingForm(HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す
        model.addAttribute("PassMissingForm", new PassMissingForm());
        return "auth/passmissing";
    }

    @PostMapping("/passmissing")
    public String handlePassMissing(@RequestParam("email") String email, HttpServletRequest httpServletRequest, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す

        User user = userService.getUserByEmail(email);
        if (user != null) {
            String requestUrl = httpServletRequest.getRequestURL().toString();
            passmissingEventPublisher.publishPassmissingEvent(user, requestUrl);

            redirectAttributes.addFlashAttribute("message", "メールを送信しました。メール本文のリンクをクリックしてパスワードを再登録してください。");
            return "redirect:/auth/passmissing";
        } else {
            redirectAttributes.addFlashAttribute("error", "そのメールアドレスは登録されていません。");
            return "redirect:/auth/passmissing";
        }
    }

    @GetMapping("/auth/passmissing")
    public String showPassMissingPage(HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す
        return "auth/passmissing";
    }

    @GetMapping("/passmissing/verify")
    public String passVerify(@RequestParam(name = "token") String token, HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す

        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);

        if (verificationToken != null) {
            model.addAttribute("token", token);
            return "auth/resetPassword";
        } else {
            model.addAttribute("errorMessage", "無効なトークンです。もう一度お試しください。");
            return "auth/error";
        }
    }

    @PostMapping("/passmissing/confirm")
    public String confirmResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       HttpSession session, Model model) {
        addUserToModel(session, model);  // セッションからユーザー情報を取得して渡す

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "パスワードが一致しません。再度確認してください。");
            return "auth/resetPassword";
        }

        boolean resetSuccess = userService.resetPassword(token, password);

        if (resetSuccess) {
            model.addAttribute("successMessage", "パスワードが正常にリセットされました。");
            return "auth/login";
        } else {
            model.addAttribute("errorMessage", "パスワードリセットに失敗しました。もう一度お試しください。");
            return "auth/resetPassword";
        }
    }
}
