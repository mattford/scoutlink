package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.utils.MircColors;

public class Message {

	private String text;
    private String sender;
    private Date timestamp;
    private Integer colour;
    private Integer backgroundColour;


    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp for this message
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    private int alignment;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_CENTER = 2;
	
	public Message (String text) {
		this.text = text;
        this.timestamp = new Date();
        setAlignment(ALIGN_CENTER);
	}

    public Message (String sender, String text) {
        this.text = text;
        this.sender = sender;
        this.alignment = ALIGN_LEFT;
        //this.backgroundColour = Color.LTGRAY;
        this.colour = Color.BLACK;
        this.timestamp = new Date();
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

    public Integer getColour() {
        return this.colour;
    }

    public void setColour(Integer colour) {
        this.colour = colour;
    }

	public LinearLayout renderTextView(Context context) {
        LayoutInflater li = LayoutInflater.from(context);
		LinearLayout view;

        SpannableString text = MircColors.toSpannable(getText());

        if (getSender() != null) {
            view = (LinearLayout) li.inflate(R.layout.message_list_item, null);
            TextView senderView = (TextView)view.findViewById(R.id.sender);
            senderView.setText(getSender());
        } else {
            view = (LinearLayout) li.inflate(R.layout.message_list_item_no_sender, null);
        }

        TextView messageView = (TextView)view.findViewById(R.id.message);
		messageView.setText(text);

        if (getAlignment() == ALIGN_RIGHT) {
            view.setGravity(Gravity.END);
            messageView.setGravity(Gravity.END);
        } else if (getAlignment() == ALIGN_CENTER) {
            ViewGroup.LayoutParams params = messageView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            messageView.setLayoutParams(params);
            view.setGravity(Gravity.CENTER);
            messageView.setGravity(Gravity.CENTER);
        }

        if (colour != null) {
            messageView.setTextColor(colour);
        }

        if (backgroundColour != null) {
            GradientDrawable bg = (GradientDrawable) messageView.getBackground();
            bg = (GradientDrawable)bg.mutate();
            bg.setColor(backgroundColour);
        }

        if (getTimestamp() != null) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            String dateString = timeFormat.format(getTimestamp());

            TextView timestampView = (TextView)view.findViewById(R.id.timestamp);
            timestampView.setText(dateString);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView ts = (TextView)view.findViewById(R.id.timestamp);
                    if (ts.getVisibility() == TextView.VISIBLE) {
                        Animation out = new AlphaAnimation(1.0f, 0.0f);
                        ts.startAnimation(out);
                        ts.setVisibility(TextView.GONE);
                    } else {
                        Animation in = new AlphaAnimation(0.0f, 1.0f);
                        ts.startAnimation(in);
                        ts.setVisibility(TextView.VISIBLE);
                    }
                }
            });
        }

		return view;
	}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Integer getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(Integer backgroundColour) {
        this.backgroundColour = backgroundColour;
    }
}
