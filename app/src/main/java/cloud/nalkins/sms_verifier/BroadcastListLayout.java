package cloud.nalkins.sms_verifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;


/**
 *
 */
class BroadcastListLayout {
    private TextView broadcastListNameText;
    private TextView EventNameText;
    private ToggleButton toggle;

    private View view;

    /**
     * DynamicLayoutMagnet constructor
     *
     * @param context the context from the layout this object was called
     */
    BroadcastListLayout(Context context, String broadcastListName, String eventName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.view = inflater.inflate(R.layout.broadcast_list, null);

        this.broadcastListNameText = view.findViewById(R.id.broadcast_name_var);
        this.broadcastListNameText.setText(broadcastListName);

        this.EventNameText = view.findViewById(R.id.event_name_var);
        EventNameText.setText(eventName);

        // Initialize the layouts main toggle button
        this.toggle = view.findViewById(R.id.br_list_toggle_button);

    }

    // Function return the View object of relevant layout
    View getView() {
        return this.view;
    }

    void setBroadcastToggleButton(boolean checked) {
        this.toggle.setChecked(checked);
    }

    ToggleButton getBroadcastToggleButton() {
        return this.toggle;
    }
}
