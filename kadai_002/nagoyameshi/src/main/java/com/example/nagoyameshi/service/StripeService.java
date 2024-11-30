package com.example.nagoyameshi.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.Token;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionUpdateParams;

@Service
public class StripeService {

    // application.properties からStripeのAPIキーを取得
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    private final UserRepository userRepository;

    // コンストラクタ
    public StripeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // @PostConstruct アノテーションでAPIキーを設定
    @PostConstruct
    public void init() {
        System.out.println("Stripe API Key in @PostConstruct: " + stripeApiKey);  // ここで値が取得できるか確認
        Stripe.apiKey = stripeApiKey;  // APIキーをセット
    }

    // サブスクリプションを作成するメソッド
    @Transactional
    public Subscription createSubscription(User user, String paymentMethodId) throws StripeException {
        System.out.println("createSubscription が呼び出されました");

        // トークンIDを使って一度だけPaymentMethodを作成
        PaymentMethod paymentMethod = convertTokenToPaymentMethod(paymentMethodId);
        
        // ユーザーのサブスクリプションIDが存在しない場合
        if (user.getStripeSubscriptionId() == null || !user.getStripeSubscriptionId().startsWith("sub_")) {
            String customerId = user.getStripeCustomerId();
            System.out.println("カスタマーID: " + customerId);

            // 顧客IDが保存されていなければ新規顧客を作成
            if (customerId == null) {
                System.out.println("顧客IDがnullです");
                Customer customer = createStripeCustomer(user);
                System.out.println("作成した顧客ID: " + customer.getId());
                customerId = customer.getId();
                user.setStripeCustomerId(customerId);
                userRepository.save(user);
            }

            // 支払い方法を顧客にアタッチ（PaymentMethodオブジェクトを使い回す）
            attachPaymentMethodToCustomer(customerId, paymentMethod.getId());

            // サブスクリプションを作成
            Subscription subscription = createStripeSubscription(customerId, paymentMethod); // 修正箇所
            user.setStripeSubscriptionId(subscription.getId()); // サブスクリプションIDを保存
            userRepository.save(user);

            return subscription; // 作成したサブスクリプションを返す
        } else {
            // すでにサブスクリプションIDが存在する場合はサブスクリプションを更新
            attachPaymentMethodToCustomer(user.getStripeCustomerId(), paymentMethod.getId());
            return updateOrCreateSubscription(user.getStripeCustomerId(), paymentMethod);
        }
    }

    // 新しいStripe顧客を作成するメソッド
    private Customer createStripeCustomer(User user) throws StripeException {
        // StripeのAPIキーが設定されているか確認
        if (Stripe.apiKey == null || Stripe.apiKey.isEmpty()) {
            throw new IllegalStateException("Stripe API key is not set.");
        }

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getName())
                .setPhone(user.getPhoneNumber())
                .build();

            return Customer.create(params);
        } catch (StripeException e) {
            // エラーハンドリング
            e.printStackTrace();
            throw e;  // StripeExceptionをそのままスロー
        }
    }
    
    // トークンIDをPaymentMethodに変換するメソッド
    private PaymentMethod convertTokenToPaymentMethod(String tokenId) throws StripeException {
        System.out.println("convertTokenToPaymentMethodが呼び出されました");
        
        // トークンIDを使ってPaymentMethodを作成
        Map<String, Object> params = new HashMap<>();
        params.put("type", "card");
        params.put("card", Collections.singletonMap("token", tokenId));  // トークンIDを渡す

        // PaymentMethodを作成
        return PaymentMethod.create(params);
    }

    // 顧客にPaymentMethodをアタッチするメソッド
    private void attachPaymentMethodToCustomer(String customerId, String paymentMethodId) throws StripeException {
        System.out.println("attachPaymentMethodToCustomerが呼び出されました");
        
        // 支払い方法を取得
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        // PaymentMethodが既に顧客にアタッチされている場合はスキップ
        if (paymentMethod.getCustomer() == null || !paymentMethod.getCustomer().equals(customerId)) {
            // PaymentMethodを顧客にアタッチ
            System.out.println("PaymentMethodを顧客にアタッチします");
            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();
            paymentMethod.attach(attachParams);
        }

        // 顧客のデフォルト支払い方法を設定
        System.out.println("顧客のデフォルト支払い方法を設定します");
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("invoice_settings", Map.of("default_payment_method", paymentMethodId));

        Customer customer = Customer.retrieve(customerId);
        customer.update(customerParams);
    }

    // createStripeSubscription メソッドを修正して、PaymentMethod を直接受け取るようにする
    private Subscription createStripeSubscription(String customerId, PaymentMethod paymentMethod) throws StripeException {
        System.out.println("createStripeSubscriptionを呼び出しました");

        if (customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty.");
        }

        // サブスクリプション作成
        SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
            .setCustomer(customerId)  // 顧客ID
            .setDefaultPaymentMethod(paymentMethod.getId());  // 支払い方法ID

        // 価格ID（Stripeの価格ID）をリストに追加
        System.out.println("価格IDをリストに追加します");
        paramsBuilder.addItem(
            SubscriptionCreateParams.Item.builder()
                .setPrice("price_1QLGzv05GRo9Sttl8CW8RSb5")  // 価格ID
                .build()
        );

        // サブスクリプションを作成して返す
        return Subscription.create(paramsBuilder.build());
    }

    // サブスクリプションをキャンセルするメソッド
    public boolean cancelSubscription(String stripeSubscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            Subscription canceledSubscription = subscription.cancel();
            return "canceled".equals(canceledSubscription.getStatus());
        } catch (StripeException e) {
            // エラーハンドリング
            e.printStackTrace();
            return false;  // エラーが発生した場合はfalseを返す
        }
    }

    // サブスクリプションを更新または作成するメソッド
    private Subscription updateOrCreateSubscription(String customerId, PaymentMethod paymentMethod) throws StripeException {
        Subscription existingSubscription = getExistingSubscription(customerId);

        if (existingSubscription != null) {
            return updateSubscription(existingSubscription.getId(), paymentMethod.getId());
        } else {
            return createStripeSubscription(customerId, paymentMethod);
        }
    }

    // 既存のサブスクリプションを取得するメソッド
    private Subscription getExistingSubscription(String customerId) throws StripeException {
        SubscriptionCollection subscriptions = Subscription.list(
            SubscriptionListParams.builder()
                .setCustomer(customerId)
                .build()
        );

        return subscriptions.getData().isEmpty() ? null : subscriptions.getData().get(0);
    }

    // サブスクリプションを更新するメソッド
    private Subscription updateSubscription(String subscriptionId, String paymentMethodId) throws StripeException {
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
            .setDefaultPaymentMethod(paymentMethodId)
            .build();

        return Subscription.retrieve(subscriptionId).update(params);
    }
    
  // ユーザーのクレジットカード情報を更新するメソッド
    public boolean updatePaymentMethod(User user, String stripeToken) {
        System.out.println("Updating payment method for user: " + user.getEmail());

        try {
            // Stripe APIの初期化
            Stripe.apiKey = stripeApiKey;

            // ユーザーのStripe Customer IDを取得
            String customerId = user.getStripeCustomerId();

            if (customerId == null) {
                return false;  // 顧客IDがない場合は処理できません
            }

            // トークンがすでに使用されているか確認
            Token token = Token.retrieve(stripeToken);
            System.out.println("Stripe Token Info: " + token.toString());

            // トークンが使用済みかどうかをログに出力
            if (token.getUsed()) {
                System.out.println("The token has already been used.");
            } else {
                System.out.println("The token has not been used yet.");
            }

            // トークンIDを使って一度だけPaymentMethodを作成
            PaymentMethod paymentMethod = convertTokenToPaymentMethod(stripeToken);

            // レスポンスのログ
            System.out.println("Stripe API Response: " + paymentMethod.toString());

            // 顧客に新しいカードを関連付ける
            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)  // 顧客IDを設定
                    .build();

            paymentMethod.attach(attachParams);  // 新しいカードを顧客に関連付け
            
            String paymentMethodId = paymentMethod.getId();

            // 顧客のデフォルト支払い方法を設定
            System.out.println("顧客のデフォルト支払い方法を設定します");
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("invoice_settings", Map.of("default_payment_method", paymentMethodId));

            Customer customer = Customer.retrieve(customerId);
            customer.update(customerParams);

            // 新しいカードを顧客のデフォルト支払い方法に設定
            customer.setDefaultSource(paymentMethod.getId());

            System.out.println("Payment method updated successfully.");

            return true;  // 支払い方法の更新に成功
        } catch (StripeException e) {
            e.printStackTrace();
            System.err.println("Stripe API call failed: " + e.getMessage());
            return false;  // 失敗した場合
        }
    }

    
    
}
