package com.invisiblearchitecture.scraper;

import java.io.IOException;

public abstract class Interpreter {
	protected final String[] sourceAttributes;
	protected final PatternInterface pattern;
	protected final String destinationField;

	public Interpreter(String[] sfs, PatternInterface p, String df) {
		sourceAttributes = sfs;
		pattern = p;
		destinationField = df;
	}
	
	public static class ToField extends Interpreter {
		private int matchNumber;
		public ToField(String sf, PatternInterface pat, int mn, String df) {
			super(new String[] {sf}, pat, df);
			matchNumber = mn;
		}
		public ToField(String[] sfs, PatternInterface pat, int mn, String df) {
			super(sfs, pat, df);
			matchNumber = mn;
		}
		public Boolean read(Information sourceInformation) {
			String result;
			String input = getInput(sourceInformation);
			if(input == null)
				return null;
			if(pattern != null) {
				result = pattern.match(input, matchNumber);
			} else {
				result = input;
			}
			if(result != null) {
				sourceInformation.putField(destinationField, result);
				return true;
			} else {
				return false;
			}
		}
		public String toString() {
			return "(" + Utils.join(sourceAttributes, ", ") + ") -> " + destinationField;
		}
	}
	
	public static class ToInformation extends Interpreter {
		private final InformationFactory factory;
		private final String targetArea;
		private final String targetInfo;
		public ToInformation(InformationFactory iF, String sf, PatternInterface pat, String target_area, String target_info, String df) {
			super(new String[] {sf}, pat, df);
			targetArea = target_area;
			targetInfo = target_info;
			factory = iF;
		}
		public ToInformation(InformationFactory iF, String[] sfs, PatternInterface pat, String target_area, String target_info, String df) {
			super(sfs, pat, df);
			targetArea = target_area;
			targetInfo = target_info;
			factory = iF;
		}
		public Boolean read(Information sourceInformation) {
			String[] results;
			String input = getInput(sourceInformation);
			if(input == null)
				return null;
			if(pattern != null) {
				results = pattern.allMatches(input);
			} else {
				results = new String[1];
				results[0] = input;
			}
			if(results == null) {
				return false;
			} else {
				Information[] childInformations = new Information[results.length];
				for(int i = 0; i < results.length; i++) {
					try {
						childInformations[i] = factory.get(targetArea, targetInfo);
						childInformations[i].putField(destinationField, results[i]);
						childInformations[i].interpret(); // We don't recursively collect here -- that's the publisher's job.	

					} catch(Exception e) {
						//logger.e("Error creating child information.", e);
						return false;
					}
				}
				// De-type inside the child listing.
				sourceInformation.addChildInformations(targetInfo, childInformations);
				return true;
			}
		}
		public String toString() {
			return "(" + Utils.join(sourceAttributes, ", ") + ") possible attributes -> Information " + targetArea + " / " + targetInfo + ": " + destinationField;
		}
	}
	
	// Uses the first sourceAttribute that's available.
	protected String getInput (Information sourceInformation) {
		for(int i = 0; i < sourceAttributes.length; i++) {
			
			String input = sourceInformation.getField(sourceAttributes[i]);
			if(input != null)
				return input;
		}
		return null;
	}
	/**
	 * Have an interpreter attempt to read Information.
	 * @param sourceInformation
	 * @return True if successful, False if unsuccessful, Null if the information isn't ready yet.
	 */
	public abstract Boolean read(Information sourceInformation);
	
	public String patternString() {
		if(pattern == null) {
			return "[No Pattern]";
		} else {
			return pattern.toString();
		}
	}
}
