package ch.epfl.hci.healthytogether;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Pledge implements Comparable<Pledge> {
	public String date;
	public int amount;
	
	public Pledge() {
		amount=0;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date = formatter.format(cal.getTime());
	}

	public Pledge(String date, int amount) {
		this.date=date;
		this.amount=amount;
	}
	
	public int compareTo(Pledge p) {
		return date.compareTo(p.date);
	}
		
	public String toString() {
		return "Pledge*** Date&Time: " + date + "\t amount: " + amount;
	}

}
