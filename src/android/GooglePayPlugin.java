package com.orbocare.cordova.plugin;
// The native Toast API
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
// Cordova-required packages
import com.google.android.gms.wallet.AutoResolveHelper;
import android.support.annotation.NonNull;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.apache.cordova.PluginResult;
import com.google.android.gms.tasks.Task;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Optional;
import org.json.JSONObject;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

public class GooglePayPlugin extends CordovaPlugin {
  private static final String DURATION_LONG = "long";
  private PaymentsClient mPaymentsClient;
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 42;

  // @Override
  // public boolean execute(String action, JSONArray args,
  //   final CallbackContext callbackContext) {
  //     // Verify that the user sent a 'show' action
  //     if (!action.equals("show")) {
  //       callbackContext.error("\"" + action + "\" is not a recognized action.");
  //       return false;
  //     }
  //
  //     String message;
  //     String duration;
  //     try {
  //       JSONObject options = args.getJSONObject(0);
  //       message = options.getString("message");
  //       duration = options.getString("duration");
  //     } catch (JSONException e) {
  //       callbackContext.error("Error encountered: " + e.getMessage());
  //       return false;
  //     }
  //     // Create the toast
  //     Toast toast = Toast.makeText(cordova.getActivity(), message,
  //       DURATION_LONG.equals(duration) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
  //     // Display toast
  //     toast.show();
  //     // Send a positive result to the callbackContext
  //     PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
  //     callbackContext.sendPluginResult(pluginResult);
  //     return true;
  // }
  //boolean keepRunning = true;

  @Override
public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    // your init code here
    cordova.setActivityResultCallback(this);

}

CallbackContext callbackCon;
String totalPrice;
String currencyCode;
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {
      // Verify that the user sent a 'show' action
      if (!action.equals("show")) {
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
      }

      try {
        JSONObject options = args.getJSONObject(0);
        totalPrice = options.getString("totalPrice");
        currencyCode = options.getString("currencyCode");
      } catch (JSONException e) {
        callbackContext.error("Error encountered: " + e.getMessage());
        return false;
      }
      // Create the toast
      mPaymentsClient =Wallet.getPaymentsClient(cordova.getActivity(),
                  new Wallet.WalletOptions.Builder()
                      .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                      .build());
      callbackCon = callbackContext;
      loadData();
      // Send a positive result to the callbackContext

      return true;
  }
//   @Override
// public void setActivityResultCallback(CordovaPlugin plugin) {
//     activityResultCallback = plugin;
// }
//
// public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
//     this.activityResultCallback = command;
//     this.activityResultKeepRunning = keepRunning;
//
//     // If multitasking turned on, then disable it for activities that return results
//     if (command != null) {
//         keepRunning = false;
//     }
//
//     // Start activity
//     super.startActivityForResult(intent, requestCode);
// }
  private void echo(String message, CallbackContext callbackContext) {
           if (message != null && message.length() > 0) {
               callbackContext.success(message);
           } else {
               callbackContext.error("Expected one non-empty string argument.");
           }
       }
  private void loadData(){
  //  possiblyShowGooglePayButton();
    //final JSONObject isReadyToPayJson = getIsReadyToPayRequest();
    final JSONObject isReadyToPayJson = getIsReadyToPayRequest();


    // if (!isReadyToPayJson.isPresent()) {
    //   return;
    // }
    IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());

    if (request == null) {
      return;
    }
    //Task<Boolean> task = mPaymentsClient.isReadyToPay(request);

    requestPayment();
  }


  /**
   * Create a Google Pay API base request object with properties used in all requests
   *
   * @return Google Pay API base request object
   * @throws JSONException
   */
  private static JSONObject getBaseRequest() throws JSONException {
    return new JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0);
  }

  /**
   * Identify your gateway and your app's gateway merchant identifier
   *
   * <p>The Google Pay API response will return an encrypted payment method capable of being charged
   * by a supported gateway after payer authorization
   *
   * <p>TODO: check with your gateway on the parameters to pass
   *
   * @return payment data tokenization for the CARD payment method
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethodTokenizationSpecification">PaymentMethodTokenizationSpecification</a>
   */
  private  JSONObject getTokenizationSpecification() throws JSONException {
    JSONObject tokenizationSpecification = new JSONObject();
    tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
    tokenizationSpecification.put(
        "parameters",
        new JSONObject()
            .put("gateway", "example")
            .put("gatewayMerchantId", "exampleGatewayMerchantId"));

    return tokenizationSpecification;
  }

  /**
   * Card networks supported by your app and your gateway
   *
   * <p>TODO: confirm card networks supported by your app and gateway
   *
   * @return allowed card networks
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
   */
  private static JSONArray getAllowedCardNetworks() {
    return new JSONArray()
        .put("AMEX")
        .put("DISCOVER")
        .put("JCB")
        .put("MASTERCARD")
        .put("VISA");
  }

  /**
   * Card authentication methods supported by your app and your gateway
   *
   * <p>TODO: confirm your processor supports Android device tokens on your supported card networks
   *
   * @return allowed card authentication methods
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#CardParameters">CardParameters</a>
   */
  private  JSONArray getAllowedCardAuthMethods() {
    return new JSONArray()
        .put("PAN_ONLY")
        .put("CRYPTOGRAM_3DS");
  }

  /**
   * Describe your app's support for the CARD payment method
   *
   * <p>The provided properties are applicable to both an IsReadyToPayRequest and a
   * PaymentDataRequest
   *
   * @return a CARD PaymentMethod object describing accepted cards
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
   */
  private  JSONObject getBaseCardPaymentMethod() throws JSONException {
    JSONObject cardPaymentMethod = new JSONObject();
    cardPaymentMethod.put("type", "CARD");
    cardPaymentMethod.put(
        "parameters",
        new JSONObject()
            .put("allowedAuthMethods", getAllowedCardAuthMethods())
            .put("allowedCardNetworks", getAllowedCardNetworks()));

    return cardPaymentMethod;
  }

  /**
   * Describe the expected returned payment data for the CARD payment method
   *
   * @return a CARD PaymentMethod describing accepted cards and optional fields
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentMethod">PaymentMethod</a>
   */
  private JSONObject getCardPaymentMethod() throws JSONException {
    JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
    cardPaymentMethod.put("tokenizationSpecification", getTokenizationSpecification());

    return cardPaymentMethod;
  }

  /**
   * Provide Google Pay API with a payment amount, currency, and amount status
   *
   * @return information about the requested payment
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#TransactionInfo">TransactionInfo</a>
   */
  private  JSONObject getTransactionInfo() throws JSONException {
    JSONObject transactionInfo = new JSONObject();
    transactionInfo.put("totalPrice", totalPrice);
    transactionInfo.put("totalPriceStatus", "FINAL");
    transactionInfo.put("currencyCode", currencyCode);

    return transactionInfo;
  }

  /**
   * Information about the merchant requesting payment information
   *
   * @return information about the merchant
   * @throws JSONException
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#MerchantInfo">MerchantInfo</a>
   */
  private  JSONObject getMerchantInfo() throws JSONException {
    return new JSONObject()
        .put("merchantName", "Example Merchant");
  }

  /**
   * An object describing accepted forms of payment by your app, used to determine a viewer's
   * readiness to pay
   *
   * @return API version and payment methods supported by the app
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#IsReadyToPayRequest">IsReadyToPayRequest</a>
   */
   public  JSONObject getIsReadyToPayRequest() {
     try {
       JSONObject isReadyToPayRequest = getBaseRequest();
       isReadyToPayRequest.put(
           "allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
       return isReadyToPayRequest;
     } catch (JSONException e) {
       return null;
     }
   }

  /**
   * An object describing information requested in a Google Pay payment sheet
   *
   * @return payment data expected by your app
   * @see <a
   *     href="https://developers.google.com/pay/api/android/reference/object#PaymentDataRequest">PaymentDataRequest</a>
   */
  public JSONObject getPaymentDataRequest() {
    try {
      JSONObject paymentDataRequest = getBaseRequest();
      paymentDataRequest.put(
          "allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
      paymentDataRequest.put("transactionInfo", getTransactionInfo());
      paymentDataRequest.put("merchantInfo", getMerchantInfo());
      return paymentDataRequest;
    } catch (JSONException e) {
      return null;
    }
  }
  public void requestPayment() {
      JSONObject paymentDataRequestJson = getPaymentDataRequest();
      // if (!paymentDataRequestJson.isPresent()) {
      //   return;
      // }

    PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
    mPaymentsClient.loadPaymentData(request);
    if (request!=null) {
      AutoResolveHelper.resolveTask(mPaymentsClient.loadPaymentData(request), cordova.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);

    }
    }

    @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
    PluginResult pluginResult;
    switch (requestCode) {
        // value passed in AutoResolveHelper
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        switch (resultCode) {
          case Activity.RESULT_OK:
            PaymentData paymentData = PaymentData.getFromIntent(data);
            String json = paymentData.toJson();
            callbackCon.success(json);

             pluginResult = new PluginResult(PluginResult.Status.OK,"1");
            callbackCon.sendPluginResult(pluginResult);
            break;
          case Activity.RESULT_CANCELED:
          //  pluginResult = new PluginResult(PluginResult.Status.NO_RESULT,"2");
          // callbackCon.sendPluginResult(pluginResult);
          callbackCon.error("Cancelled");

            break;
          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            //  pluginResult = new PluginResult(PluginResult.Status.ERROR,"1");
            // callbackCon.sendPluginResult(pluginResult);
            // Log the status for debugging.
            // Generally, there is no need to show an error to the user.
            // The Google Pay payment sheet will present any account errors.
            callbackCon.error(status.toString());

            break;
          default:
          //  pluginResult = new PluginResult(PluginResult.Status.ERROR,"1");
          // callbackCon.sendPluginResult(pluginResult);
          callbackCon.error("failed");
          break;
            // Do nothing.
        }
        break;
      default:
        // Do nothing.
    }

  }
}
