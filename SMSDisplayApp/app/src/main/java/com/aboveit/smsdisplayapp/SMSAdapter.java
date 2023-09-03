package com.aboveit.smsdisplayapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.ViewHolder> {
    private List<SMSMessage> messages;
    private Context context;

    public SMSAdapter(List<SMSMessage> messages) {
        this.messages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SMSMessage message = messages.get(position);
        holder.senderTextView.setText(Utility.getContactName(message.getSender(), context, false));
        holder.messageTextView.setText(message.getMessage());
        holder.timeTextView.setText(Utility.formatTimestamp(message.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView senderTextView;
        public TextView messageTextView;
        public TextView timeTextView;

        public ViewHolder(View view) {
            super(view);
            senderTextView = view.findViewById(R.id.sender_name);
            messageTextView = view.findViewById(R.id.sms_body);
            timeTextView = view.findViewById(R.id.time);
        }
    }
}
