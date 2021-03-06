package info.smemo.nbaseaction.base;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import info.smemo.nbaseaction.app.AppConstant;
import info.smemo.nbaseaction.app.AppManager;
import info.smemo.nbaseaction.ui.MaterialDialog;
import info.smemo.nbaseaction.util.view.ViewInjectUtils;

public class NBaseActivity extends Activity implements AppConstant, NBaseCommonView {

    protected ProgressDialog mProgressDialog;
    private MaterialDialog mMessageDialog;

    protected final BaseHandler mBaseHandler = new BaseHandler(this);

    private static final int SHOW_PROGRESS_DIALOG = 0x110001;

    protected void onCreateDataBinding() {

    }

    protected <T extends ViewDataBinding> T createContentView(int layout) {
        return DataBindingUtil.setContentView(this, layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateDataBinding();
        //add activity to activity manager
        AppManager.getAppManager().addActivity(this);
        ViewInjectUtils.inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
        destoryProgressDialog();
        mProgressDialog = null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    /**
     * show a progress dialog
     *
     * @param title progress notice message
     */
    @Override
    public void showProgressDialog(String title) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(title);
        }
        mProgressDialog.setMessage(title);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    @Override
    public void showProgressDialogInThread(String title) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        Message message = new Message();
        message.what = SHOW_PROGRESS_DIALOG;
        message.setData(bundle);
        mBaseHandler.sendMessage(message);
    }

    /**
     * dismiss progress dialog
     */
    @Override
    public void dismissProgressDialog() {
        NBaseActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                destoryProgressDialog();
            }
        });
    }

    @Override
    public void destoryProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case SHOW_PROGRESS_DIALOG:
                showProgressDialog(msg.getData().getString("title"));
                break;
        }
    }

    protected static class BaseHandler extends Handler {

        private final WeakReference<NBaseActivity> mBaseActivityWeakReference;

        public BaseHandler(NBaseActivity baseActivity) {
            super();
            mBaseActivityWeakReference = new WeakReference<>(baseActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NBaseActivity baseActivity = mBaseActivityWeakReference.get();
            if (null != baseActivity) {
                baseActivity.handleMessage(msg);
            }
        }
    }

    @Override
    public void showMessage(String title, String message) {
        showMessage(title, message, null, null);
    }

    @Override
    public void showMessage(String title, String message, final View.OnClickListener okClickListener, final View.OnClickListener cancelListener) {
        if (null == mMessageDialog)
            mMessageDialog = new MaterialDialog(this);
        mMessageDialog
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (okClickListener != null)
                            okClickListener.onClick(v);
                        mMessageDialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cancelListener != null)
                            cancelListener.onClick(v);
                        mMessageDialog.dismiss();
                    }
                })
                .setTitle(title)
                .setMessage(message);
        mMessageDialog.show();
    }

    @Override
    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context getVContext() {
        return this;
    }

    @Override
    public Context getVApplicationContext() {
        return getApplicationContext();
    }

    @Override
    public Application getVApplication() {
        return getApplication();
    }

    protected void injectView() {
        ViewInjectUtils.inject(this);
    }
}
