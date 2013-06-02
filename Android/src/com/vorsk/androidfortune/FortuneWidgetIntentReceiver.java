package com.vorsk.androidfortune;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.TextView;

public class FortuneWidgetIntentReceiver extends BroadcastReceiver{
	
	/** Receives the intent and checks if it is up vote or 
	 * down vote then calls the appropriate listener. Also
	 * implements a thread to re-display the fortune. 
	 * @param context state of the widget
	 * 	      intent received intent
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {
		
		if(intent.getAction().equals("up_button.intent.action.UP_VOTE")){
			updateWidgetUpButtonListener(context, "Up Vote Clicked :-D");
			
			// creating a timer thread to set text view back to the fortune String
			Thread timer = new Thread(){
				public void run(){
					try{
						sleep(600);
					} catch(InterruptedException e){
						e.printStackTrace();
					} finally{
						updateWidgetUpButtonListener(context, WidgetActivity.fortuneText);
					}
				}
			};
			timer.start();
			

		}
		else if(intent.getAction().equals("down_button.intent.action.DOWN_VOTE")){
			updateWidgetDownButtonListener(context, "Down Vote Clicked :-( ");
			
			// creating a timer thread to set text view back to the fortune String
			Thread timer = new Thread(){
				public void run(){
					try{
						sleep(600);
					} catch(InterruptedException e){
						e.printStackTrace();
					} finally{
						updateWidgetDownButtonListener(context, WidgetActivity.fortuneText);
					}
				}
			};
			timer.start();
			

		}
	}// end method
	
	/** Counts up vote as user presses up button and notify's
	 * the user on text view that they have selected up vote.
	 * @param context state of the application
	 * 		  textViewString string displayed to the user after selecting down vote
	 */
	private void updateWidgetUpButtonListener(Context context, String textViewString){
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remoteViews.setTextViewText(R.id.fortune_view, textViewString);
		
		
		// Calling countUpVote to count users up vote
		//WidgetActivity.countUpVote(true);
		
		//Refreshing Button Listener
		remoteViews.setOnClickPendingIntent(R.id.up_button, FortuneWidgetProvider.buildUpButtonPendingIntent(context));
		FortuneWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	
	}// end method
	
	/** Counts down vote as user press down button and notify's
	 * the user on text view that they have selected up vote.
	 * @param context state of the application
	 * 		  textViewString string displayed to the user after selecting down vote 
	 */
	private void updateWidgetDownButtonListener(Context context, String textViewString){
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);	
		remoteViews.setTextViewText(R.id.fortune_view, textViewString);
		
		// calling countDownVote to count users down vote
		//WidgetActivity.countDownVote(true);
	
		
		//Refreshing Button Listener
		remoteViews.setOnClickPendingIntent(R.id.down_button, FortuneWidgetProvider.buildDownButtonPendingIntent(context));
		FortuneWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	
	}// end method

}// end class
