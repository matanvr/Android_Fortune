package com.vorsk.androidfortune.widget;

import com.vorsk.androidfortune.R;
import com.vorsk.androidfortune.data.Client;
import com.vorsk.androidfortune.data.Fortune;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class FortuneWidgetIntentReceiver extends BroadcastReceiver{
	
	private static final String TAG = "WidgetIntentReciever";
	
	/** Receives the intent and checks if it is up vote or 
	 * down vote then calls the appropriate listener. Also
	 * implements a thread to re-display the fortune. 
	 * @param context Context of current state of the application
	 * @param intent received intent
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.v(TAG,"onRecieve");
		
		// creating a RemoteView 
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		// getting the current fortune from client by calling the getCurrentFortune method
		final Fortune current_fortune = Client.getInstance(context).getCurrentFortune();
		
		// checks if the current fortune is null, if it is then don't do anything 
		if ( current_fortune == null ) {
			return;
		}

		String vote_message = null;
		
		if (current_fortune != null)
		{
			/* checks if the received intent is up button intent, if it is
			 * then check if the user has up voted by calling countUpVote which commits a upVote and
			 * return true if successful and false if not successful. false means the user has already
			 * up voted and the vote was not committed. 
			 */
			if(intent.getAction().equals("up_button.intent.action.UP_VOTE")){
				if (WidgetActivity.countUpVote(context)) {
					vote_message = context.getResources().getString(R.string.up_vote);
				}else {
					vote_message = context.getResources().getString(R.string.already_voted);
				}
				remoteViews.setOnClickPendingIntent(R.id.upvote_button, FortuneWidgetProvider.buildUpButtonPendingIntent(context));
			}
			else if(intent.getAction().equals("down_button.intent.action.DOWN_VOTE")){
				if (WidgetActivity.countDownVote(context)) {
					vote_message = context.getResources().getString(R.string.down_vote);
				}else {
					vote_message = context.getResources().getString(R.string.already_voted);
				}
				remoteViews.setOnClickPendingIntent(R.id.downvote_button, FortuneWidgetProvider.buildDownButtonPendingIntent(context));
			}
			
			/* This is a thread that tells the user you have voted or already voted by
			 * setting the TextView to vote_message. The thread sleeps for 6th of a second
			 * and then sets the Text view of the widget back to the fortune
			 */ 
			if (vote_message != null)
			{
				remoteViews.setTextViewText(R.id.fortune_text, vote_message);
				// creating a timer thread to set text view back to the fortune String
				Thread timer = new Thread(){
					public void run(){
						try{
							sleep(600);
						} catch(InterruptedException e){
							Log.e(TAG,"thread exception");
						} finally{
							remoteViews.setTextViewText(R.id.fortune_text, current_fortune.getFortuneText(true));
							FortuneWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
						}
					}
				};
				timer.start();
			}else{
				Log.v(TAG, "vote message is null!");
			}
		}
		
		FortuneWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
		
	}// end method

}// end class
