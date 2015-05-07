package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.FacebookLoginResponse;
import castofo_nower.com.co.nower.helpers.ParsedErrors;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.DateManager;
import castofo_nower.com.co.nower.support.FacebookHandler;
import castofo_nower.com.co.nower.support.RequestErrorsHandler;
import castofo_nower.com.co.nower.support.UserFeedback;
import castofo_nower.com.co.nower.support.SharedPreferencesManager;


public class Login extends Activity implements SubscribedActivities,
ParsedErrors, FacebookLoginResponse {

  private TextView emailView;
  private TextView passwordView;

  private String email;
  private String password;

  private FacebookHandler facebookHandler = FacebookHandler.getInstance();
  private LoginButton loginButton;
  private CallbackManager callbackManager;

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_LOGIN = "/users/login";
  public static final String ACTION_FACEBOOK_LOGIN = "/users/login/facebook";
  private Map<String, String> params = new HashMap<String, String>();

  private RequestErrorsHandler requestErrorsHandler = new
                                                      RequestErrorsHandler();

  public static final String OPEN_MAP = "OPEN_MAP";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    httpHandler.addListeningActivity(this);

    requestErrorsHandler.addListeningActivity(this);

    facebookHandler.addListeningActivity(this);


    SharedPreferencesManager.setup(this);

    TextView title = (TextView) findViewById(R.id.login_header);
    Typeface headerFont = Typeface.createFromAsset(getAssets(),
                                                   "fonts/exo2_extra_bold.otf");
    title.setTypeface(headerFont);

    emailView = (TextView) findViewById(R.id.email);
    passwordView = (TextView) findViewById(R.id.password);

    if (getIntent().hasExtra("email")) {
      String emailFromRegister = getIntent().getExtras().getString("email");
      emailView.setText(emailFromRegister);
    }
    initializeFacebookUI();
  }

  private void initializeFacebookUI() {
    // Inicializar el SDK de Facebook.
    FacebookSdk.sdkInitialize(getApplicationContext());

    callbackManager = facebookHandler.getCallbackManagerInstance();

    loginButton = (LoginButton) findViewById(R.id.login_button);
    loginButton.setReadPermissions("public_profile, email");
    loginButton.registerCallback(callbackManager,
            facebookHandler.getLoginCallback());
  }

  public void onDontHaveAccountClicked(View v) {
    Intent intent = new Intent(Login.this, Register.class);
    intent.putExtra("email", emailView.getText().toString());
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(intent);
  }

  public void onLoginClicked(View v) {
    email = emailView.getText().toString().trim();
    password = passwordView.getText().toString().trim();

    if (email.isEmpty() && password.isEmpty()) {
      emailView.setError(getResources().getString(R.string.write_your_email));
      passwordView.setError(getResources()
                            .getString(R.string.write_your_password));
    }
    else if (email.isEmpty() && !password.isEmpty()) {
      emailView.setError(getResources().getString(R.string.write_your_email));
    }
    else if (!email.isEmpty() && password.isEmpty()) {
      passwordView.setError(getResources()
                            .getString(R.string.write_your_password));
    }
    else {
      setParamsForLogin();
    }
  }

  public void setParamsForLogin() {
    params.put("email", email);
    params.put("password", password);
    sendRequest(ACTION_LOGIN);
  }

  public static Map<String, String> setParamsForFacebookLogin
  (Map<String, String> params, String name, String email, String gender,
   String authToken, String facebookId, Date expires, JSONObject ageRange) {
    params.put("name", name);
    params.put("email", email);
    params.put("gender", gender);
    params.put("token", authToken);
    params.put("facebook_id", facebookId);
    params.put("expires", DateManager.getDateTime(expires));
    params.put("age_range", ageRange.toString());
    return params;
  }

  public void sendRequest(String request) {
    if (request.equals(ACTION_LOGIN)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_LOGIN, "", params,
                              new HttpPost(), Login.this);
    }
    else if (request.equals(ACTION_FACEBOOK_LOGIN)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_FACEBOOK_LOGIN,
                              "", params, new HttpPost(), Login.this);
    }
  }

  public static void saveUserData(int id, String email, String name,
                                  String gender, String birthday) {
    // Se almacenan los datos del usuario que acaba de autenticarse.
    SharedPreferencesManager.saveIntegerValue(SharedPreferencesManager
                                              .USER_ID, id);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
            .USER_EMAIL, email);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_NAME, name);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_GENDER, gender);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_BIRTHDAY, birthday);
    User.setUserData(id, email, name, gender, birthday);
  }

  public static void startSession
  (Context context, JSONObject responseJson) throws JSONException {
    //String token = responseJson.getString("token");
    JSONObject user = responseJson.getJSONObject("user");
    int id = user.getInt("id");
    String email = user.getString("email");
    String name = user.getString("name");
    String gender = user.getString("gender");
    String birthday = user.getString("birthday");

    saveUserData(id, email, name, gender, birthday);

    SplashActivity.handleRequest(context, OPEN_MAP);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    callbackManager.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void notifyParsedErrors(String action,
                                 Map<String, String> errorsMessages) {
    switch (action) {
      case ACTION_LOGIN:
        if (errorsMessages.containsKey("login")) {
          UserFeedback.showToastMessage(getApplicationContext(),
                                        errorsMessages.get("login"),
                                        Toast.LENGTH_SHORT);
        }
        break;
      case ACTION_FACEBOOK_LOGIN:
        // Cerrar la sesión de Facebook
        facebookHandler.logout();
        // Mostrar el primer mensaje de error que llegue.
        for (Map.Entry<String, String> errorMessage : errorsMessages.entrySet())
        {
          UserFeedback
                  .showAlertDialog(Login.this, R.string.sorry,
                          errorMessage.getValue(), R.string.got_it,
                          UserFeedback.NO_BUTTON_TO_SHOW,
                          ACTION_FACEBOOK_LOGIN);
          // Solo mostrar el primer mensaje, no más.
          break;
        }
        break;
    }
  }

  @Override
  public void notifyFacebookResponse(JSONObject object, GraphResponse response)
  {
    AccessToken accessToken = facebookHandler.getAccessToken();
    // Guardar en preferencias el token de Facebook del usuario
    SharedPreferencesManager.saveStringValue
    (SharedPreferencesManager.USER_FACEBOOK_TOKEN, accessToken.getToken());
    try {
      String name = object.getString("name");
      String email = object.getString("email");
      String gender = object.getString("gender");
      String authToken = accessToken.getToken();
      String facebookId = object.getString("id");
      Date expires = accessToken.getExpires();
      JSONObject ageRange = object.getJSONObject("age_range");
      params = setParamsForFacebookLogin(params, name, email, gender, authToken,
                                         facebookId, expires, ageRange);
      sendRequest(ACTION_FACEBOOK_LOGIN);
    }
    catch (JSONException e) {

    }
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      Log.i("responseJson", responseJson.toString());
      int responseStatusCode = responseJson.getInt(HttpHandler.HTTP_STATUS);
      if (action.equals(ACTION_LOGIN)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            if (responseJson.getBoolean("success")) {
              startSession(Login.this, responseJson);
            }
            break;
          case HttpHandler.BAD_REQUEST:
            RequestErrorsHandler
            .parseErrors(action, responseJson.getJSONObject("errors"));
            break;
        }
      }
      else if (action.equals(ACTION_FACEBOOK_LOGIN)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            if (responseJson.getBoolean("success")) {
              startSession(this, responseJson);
            }
            break;
          case HttpHandler.CREATED:
            if (responseJson.getBoolean("success")) {
              startSession(this, responseJson);
            }
            break;
          case HttpHandler.BAD_REQUEST:
            RequestErrorsHandler
            .parseErrors(action, responseJson.getJSONObject("errors"));
            break;
        }
      }

      params.clear();

    }
    catch (JSONException e) {

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_login, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
