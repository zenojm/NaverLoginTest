package com.zenolab.jmlee.naverlogintest;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;


/// 네이버 아이디로 로그인 샘플앱

/**
 * <br/> OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제,
 * <br/> 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 *
 * @author naver
 */
public class OAuthSampleActivity extends Activity {

    private static final String TAG = "OAuthSampleActivity";

    /**
     * client 정보를 넣어준다.
     */
    private static String OAUTH_CLIENT_ID     = "5rFGR091G7v_arg5lpoE";
    private static String OAUTH_CLIENT_SECRET = "g8A4RDCWw5";
    private static String OAUTH_CLIENT_NAME   = "네이버 아이디로 로그인";

    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;

    /**
     * UI 요소들
     */
    private TextView mApiResultText;
    private static TextView mOauthAT;
    private static TextView mOauthRT;
    private static TextView mOauthExpires;
    private static TextView mOauthTokenType;
    private static TextView mOAuthState;

    private OAuthLoginButton mOAuthLoginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naveroauthlogin_sample_main);


        mContext = this;


        initData();
        initView();

        this.setTitle("OAuthLoginSample Ver." + OAuthLogin.getVersion());
    }


    private void initData() {
        mOAuthLoginInstance = OAuthLogin.getInstance();

        mOAuthLoginInstance.showDevelopersLog(true);
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        /*
         * 2015년 8월 이전에 등록하고 앱 정보 갱신을 안한 경우 기존에 설정해준 callback intent url 을 넣어줘야 로그인하는데 문제가 안생긴다.
         * 2015년 8월 이후에 등록했거나 그 뒤에 앱 정보 갱신을 하면서 package name 을 넣어준 경우 callback intent url 을 생략해도 된다.
         */
        //mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_callback_intent_url);
    }

    private void initView() {
        mApiResultText = (TextView) findViewById(R.id.api_result_text);

        mOauthAT = (TextView) findViewById(R.id.oauth_access_token);
        mOauthRT = (TextView) findViewById(R.id.oauth_refresh_token);
        mOauthExpires = (TextView) findViewById(R.id.oauth_expires);
        mOauthTokenType = (TextView) findViewById(R.id.oauth_type);
        mOAuthState = (TextView) findViewById(R.id.oauth_state);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);      //네이버 로그인 아이디 버튼 뷰등록
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);     //네이버 아이디로 로그인 버튼 클릭 시, 계정연동으로 가는 핸들러

        updateView();       //onCreate
    }


    private void updateView() {
        mOauthAT.setText(mOAuthLoginInstance.getAccessToken(mContext));
        mOauthRT.setText(mOAuthLoginInstance.getRefreshToken(mContext));
        mOauthExpires.setText(String.valueOf(mOAuthLoginInstance.getExpiresAt(mContext)));
        mOauthTokenType.setText(mOAuthLoginInstance.getTokenType(mContext));
        mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
    }

    @Override
    protected void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();

    }

    /**
     * startOAuthLoginActivity() 호출시 인자로 넘기거나, OAuthLoginButton 에 등록해주면 인증이 종료되는 걸 알 수 있다.
     */
    static private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
                mOauthAT.setText(accessToken);
                mOauthRT.setText(refreshToken);
                mOauthExpires.setText(String.valueOf(expiresAt));
                mOauthTokenType.setText(tokenType);
                mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
            } else {

                //인증서버로부터 못받아오는 경우 여기서 표시된다.
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }

    };

    public void onButtonClick(View v) throws Throwable {

        switch (v.getId()) {
            case R.id.buttonOAuth: {        //인증하기
                mOAuthLoginInstance.startOauthLoginActivity(OAuthSampleActivity.this, mOAuthLoginHandler);
                break;
            }
            case R.id.buttonVerifier: {     //API호출
                new RequestApiTask().execute();
                break;
            }
            case R.id.buttonRefresh: {      //토큰다시받기
                new RefreshTokenTask().execute();
                break;
            }
            case R.id.buttonOAuthLogout: {      //로그아웃
                mOAuthLoginInstance.logout(mContext);
                updateView();       //토큰다시받기 버튼 클릭
                break;
            }
            case R.id.buttonOAuthDeleteToken: {     //연동끊기
                new DeleteTokenTask().execute();
                break;
            }
            default:
                break;
        }
    }

    //연동끊기 버튼 클릭 시, 실행되는 asynctask
    private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            boolean isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(mContext);

            if (!isSuccessDeleteToken) {
                // 서버에서 token 삭제에 실패했어도 클라이언트에 있는 token 은 삭제되어 로그아웃된 상태이다
                // 실패했어도 클라이언트 상에 token 정보가 없기 때문에 추가적으로 해줄 수 있는 것은 없음
                Log.d(TAG, "errorCode:" + mOAuthLoginInstance.getLastErrorCode(mContext));
                Log.d(TAG, "errorDesc:" + mOAuthLoginInstance.getLastErrorDesc(mContext));


                /**
                 *  연동을 끊을 때는 해당 정보를 서버에 넘겨주어 서버DB에서 연결된 정보를 삭제해주도록 한다.
                 *
                 *  연동된 사용자 네이버계정만 삭제해주도록 한다.
                 *  장치는 다른 계정과 연동될 수 있다는 전제
                 */
           }

            return null;
        }

        protected void onPostExecute(Void v) {
            updateView();       //연동끊기
        }
    }

    //api 호출 버튼 클릭 시, 실행되는 asynctask
    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            mApiResultText.setText((String) "");
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            return mOAuthLoginInstance.requestApi(mContext, at, url);
        }
        //api 호출 후, 결과값 //선택한 허용사항에 따라 출력된다. {"resultcode":"00","message":"success","response":{"id":"18284985","email":"skyloveljm@naver.com","name":"\uc774\uc815\ubbfc"}}
        protected void onPostExecute(String content) {
            mApiResultText.setText((String) content);
        }
    }

    //토큰 다시 받기 클릭 시, 실행되는 asynctask
    private class RefreshTokenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return mOAuthLoginInstance.refreshAccessToken(mContext);

            /**
             * 요청 시, 해당 토큰을 폰에서 저장하고 있는다
             * 폰에 저장된 토큰과 현재 받아온 토큰이 다르면 서버쪽에 새 토큰을 전달한다 (현재 디바이스 정보도 보내줘야 함)
             *
             * 서버측에서는 해당 모바일의 정보를 받아 토큰을 재저장한다
             */
        }
        //네이버로부터 받은 토큰(다시받을 때마다 토큰이 갱신된다) :
        //AccessToken  : AAAAOprLiCOL66nx/oEHnIk4NNmNmJYHB+UmST697RoejCT2Ii43LCYETD6QxTDc6bZPFV92gDzIQofdwAS8LBLx6oQ=
        //RefreshToken : sIPtZisjc2VIbEfPyFvypCrqWSisXWZ0ipKt7I9PAEpq2yGii0AHyjxQ08lKoYkrasXDOpXAJipRsLUOtBN4e8ekFCMrTy9j6sjboHTjwGNDQipRGlnfQKNnVipnjJ6wg2LLTEis
        protected void onPostExecute(String res) {
            updateView();           //토큰다시받기 클릭
        }
    }
}