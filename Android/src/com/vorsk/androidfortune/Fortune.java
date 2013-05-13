package com.vorsk.androidfortune;

import java.util.Date;

import android.database.Cursor;

public class Fortune implements Comparable<Fortune> {
	
	private int fortuneID;
	private String fortuneText;
	private int upvotes = 0;
	private boolean upvoted = false;
	private boolean downvoted = false;
	private int downvotes = 0;
	private boolean flagged = false;
	private boolean owner; //true if the user created the fortune
	private Date seen;
	private Date submitted;
	

	public Fortune(String str)
	{
		//TODO construct from json from server
	}
	
	public Fortune(Cursor c)
	{
		fortuneID = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_ID));
		fortuneText = c.getString(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_TEXT));
		upvotes = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_UPVOTES));
		upvoted = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_UPVOTED))!=0;
		downvotes = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_DOWNVOTES));
		downvoted = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_DOWNVOTED))!=0;
		flagged = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_FLAG))!=0 ;
		owner = c.getInt(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_OWNER))!=0 ;
		
		long tempTime = c.getLong(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_VIEWDATE));
		if ( tempTime > 0 ) {
			seen = new Date(tempTime*1000);
		}
		submitted = new Date( c.getLong(c.getColumnIndexOrThrow(FortuneDbAdapter.KEY_SUBMITDATE))*1000);
		
	}
	
	/*
	 * User submitted fortune
	 */
	public Fortune(String fortune, Date date)
	{
		fortuneText = fortune;
		seen = submitted = date; // current time when created
		owner = true;
	}
	
	public boolean hasVoted()
	{
		return this.upvoted || this.downvoted;
	}
	
	public void flag()
	{
		if (!this.flagged)
		{
			this.flagged = true;
			FortuneDbAdapter.getInstance().updateFortuneCol(getFortuneID(),
					FortuneDbAdapter.KEY_FLAG, getFlagged());
		}
	}
	
	public void upvote()
	{
		if (!this.hasVoted())
		{
			this.upvoted = true;
			this.upvotes++;
			//TODO call client.submitVote(this,true);
		}
	}
	
	public void downvote()
	{
		if (!this.hasVoted())
		{
			this.downvoted = true;
			this.downvotes++;
			//TODO call client.submitVote(this,false);
		}
	}
	
	
	public String getFortuneText()
	{
		if (this.seen == null)
		{
			//update the date to the current time
			this.seen = new Date();
			FortuneDbAdapter.getInstance().updateFortuneCol(fortuneID, 
					FortuneDbAdapter.KEY_VIEWDATE, this.seen);
		}
		return this.fortuneText;
	}
	
	public Date getDate()
	{
		if (this.seen == null) {
			this.seen = new Date();
			FortuneDbAdapter.getInstance().updateFortuneCol(fortuneID, 
					FortuneDbAdapter.KEY_VIEWDATE, this.seen);
		}
		return this.seen;
	}

	@Override
	public int compareTo(Fortune another) {
		if (this.seen == null)
		{
			return -1;
		}
		if (another.seen == null)
		{
			return 1;
		}
		return this.seen.compareTo(another.seen);
	}
	
	//Getters
	public int getFortuneID() { return fortuneID; }
	public int getUpvotes() { return upvotes; }
	public boolean getUpvoted() { return upvoted; }
	public int getDownvotes() { return downvotes; }
	public boolean getDownvoted() { return downvoted; }
	public boolean getFlagged() { return flagged; }
	public boolean getOwner() { return owner; }
	public Date getSeen() { return seen; }
	public Date getSubmitted() { return submitted; }
	
}
