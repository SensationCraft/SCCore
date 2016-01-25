package com.sensationcraft.sccore.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Anml on 1/9/16.
 */
public class Utils {

	public long longLength(String length) {
		length = this.actualLength(length);
		long amount = 0;
		String temp = "";

		for (char c : length.toLowerCase().toCharArray()) {
			if (Character.isDigit(c)) {
				temp += c;
			} else {
				if (temp.length() != 0) {
					switch (c) {
					case 'd':
						amount += (Integer.parseInt(temp) * 86400000);
						break;
					case 'h':
						amount += (Integer.parseInt(temp) * 3600000);
						break;
					case 'm':
						amount += (Integer.parseInt(temp) * 60000);
						break;
					case 's':
						amount += (Integer.parseInt(temp) * 1000);
						break;
					}
					temp = "";
				}
			}
		}

		return amount;
	}

	public String actualLength(String length) {
		long days = 0;
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		String temp = "";

		for (char c : length.toLowerCase().toCharArray()) {
			if (Character.isDigit(c)) {
				temp += c;
			} else {
				if (temp.length() != 0) {
					switch (c) {
					case 'd':
						days += (Integer.parseInt(temp));
						break;
					case 'h':
						hours += (Integer.parseInt(temp));
						break;
					case 'm':
						minutes += (Integer.parseInt(temp));
						break;
					case 's':
						seconds += (Integer.parseInt(temp));
						break;
					}
					temp = "";
				}
			}
		}

		temp = "";
		temp += days != 0 ? days + "d" : "";
		temp += hours != 0 ? hours + "h" : "";
		temp += minutes != 0 ? minutes + "m" : "";
		temp += seconds != 0 ? seconds + "s" : "";

		return temp;
	}

	public String getDifference(long start, long end) {


		Date dateStart = new Date(start);
		Date dateStop = new Date(end);

		long diff = dateStop.getTime() - dateStart.getTime();
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000);
		int diffDays = (int) diff / (1000 * 60 * 60 * 24);

		String returned = "";

		if (diffDays != 0) returned += diffDays + "d";
		if (diffHours != 0) returned += (int) diffHours + "h";
		if (diffMinutes != 0) returned += (int) diffMinutes + "m";
		if (diffSeconds != 0) returned += (int) diffSeconds + "s";

		return returned;
	}

	public String getTimeStamp(long l) {
		final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("EST"));
		return DATE_FORMAT.format(new Date(l)) + " EST";
	}

	public String getDateStamp(long l) {
		final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy");
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("EST"));
		return DATE_FORMAT.format(new Date(l));
	}
}
