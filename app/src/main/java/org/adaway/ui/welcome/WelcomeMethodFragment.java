package org.adaway.ui.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import org.adaway.R;
import org.adaway.helper.PreferenceHelper;

import static android.app.Activity.RESULT_OK;
import static org.adaway.model.adblocking.AdBlockMethod.ROOT;
import static org.adaway.model.adblocking.AdBlockMethod.VPN;

/**
 * This class is a fragment to setup the ad blocking method.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class WelcomeMethodFragment extends Fragment {
    private static final int VPN_START_REQUEST_CODE = 10;
    private CardView rootCardView;
    private CardView vpnCardView;
    @ColorInt
    private int cardColor;
    @ColorInt
    private int cardEnabledColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome_method_layout, container, false);

        this.rootCardView = view.findViewById(R.id.rootCardView);
        this.rootCardView.setOnClickListener(this::checkRoot);
        this.vpnCardView = view.findViewById(R.id.vpnCardView);
        this.vpnCardView.setOnClickListener(this::enableVpnService);

        this.cardColor = getResources().getColor(R.color.cardBackground, null);
        this.cardEnabledColor = getResources().getColor(R.color.cardEnabledBackground, null);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check VPN activation request
        if (requestCode == VPN_START_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                notifyVpnEnabled();
            } else {
                notifyVpnDisabled();
            }
        }
    }

    private void checkRoot(@SuppressWarnings("unused") @Nullable View view) {
        notifyVpnDisabled();
        if (Shell.rootAccess()) {
            notifyRootEnabled();
        } else {
            notifyRootDisabled(true);
        }
    }

    private void enableVpnService(@SuppressWarnings("unused") @Nullable View view) {
        notifyRootDisabled(false);
        Context context = this.getContext();
        if (context == null) {
            return;
        }
        // Check user authorization
        Intent prepareIntent = android.net.VpnService.prepare(context);
        if (prepareIntent == null) {
            notifyVpnEnabled();
        } else {
            startActivityForResult(prepareIntent, VPN_START_REQUEST_CODE);
        }
    }

    private void notifyRootEnabled() {
        Context context = this.getContext();
        if (context != null) {
            PreferenceHelper.setAbBlockMethod(context, ROOT);
        }
        this.rootCardView.setCardBackgroundColor(this.cardEnabledColor);
        this.vpnCardView.setCardBackgroundColor(this.cardColor);
        this.getNavigable().allowNext();
    }

    private void notifyRootDisabled(boolean showDialog) {
        this.rootCardView.setCardBackgroundColor(this.cardColor);
        this.vpnCardView.setCardBackgroundColor(this.cardColor);
        Context context = this.getContext();
        if (context != null && showDialog) {
            this.getNavigable().blockNext();
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.welcome_root_missing_title)
                    .setMessage(R.string.welcome_root_missile_description)
                    .setPositiveButton(R.string.button_close, null)
                    .create()
                    .show();
        }
    }

    private void notifyVpnEnabled() {
        Context context = this.getContext();
        if (context != null) {
            PreferenceHelper.setAbBlockMethod(context, VPN);
        }
        this.rootCardView.setCardBackgroundColor(this.cardColor);
        this.vpnCardView.setCardBackgroundColor(this.cardEnabledColor);
        this.getNavigable().allowNext();
    }

    private void notifyVpnDisabled() {
        this.rootCardView.setCardBackgroundColor(this.cardColor);
        this.vpnCardView.setCardBackgroundColor(this.cardColor);
    }
    
    private WelcomeNavigable getNavigable() {
        return (WelcomeNavigable) this.getActivity();
    }
}