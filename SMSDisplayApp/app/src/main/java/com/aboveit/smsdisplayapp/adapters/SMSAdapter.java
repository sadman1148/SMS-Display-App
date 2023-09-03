package com.aboveit.smsdisplayapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aboveit.smsdisplayapp.R;
import com.aboveit.smsdisplayapp.models.Message;
import com.aboveit.smsdisplayapp.utils.Constants;
import com.aboveit.smsdisplayapp.utils.ItemClickListerner;
import com.aboveit.smsdisplayapp.utils.Utility;

import java.util.List;

public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.ViewHolder> {
    private List<Message> messages;
    private Context context;
    private ItemClickListerner itemClickListerner;

    public SMSAdapter(List<Message> messages, ItemClickListerner itemClickListerner) {
        this.messages = messages;
        this.itemClickListerner = itemClickListerner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.senderTextView.setText(Utility.getContactName(message.getSender(), context, false));
        holder.messageTextView.setText(message.getMessage());
        holder.timeTextView.setText(Utility.formatTimestamp(message.getTimestamp(), Constants.TIME_PATTERN));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListerner.onItemClick(message.getSender(), message.getMessage(), message.getTimestamp());
            }
        });
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
