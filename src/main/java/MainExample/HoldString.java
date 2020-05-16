package MainExample;

public class HoldString {
	private String string;

	public HoldString(String string) {
		this.string = string;
	}

	public synchronized String getString() {
		return string;
	}

	public synchronized void setString(String string) {
		this.string = string;
	}

	public synchronized void swapStrings(String string1, String string2) {
		string = string.contains(string1) ? string.replaceAll(string1, string2) :
				string.contains(string2) ? string.replaceAll(string2, string1) : string;
	}
}
