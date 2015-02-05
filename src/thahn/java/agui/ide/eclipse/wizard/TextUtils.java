package thahn.java.agui.ide.eclipse.wizard;

public class TextUtils {
	/**
	 * Returns true if the string is null or 0-length.
	 * 
	 * @param str
	 *            the string to be examined
	 * @return true if str is null or zero length
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}

	public static boolean isEmpty(String str) {
		if (str == null || str.length() == 0 || str.trim().equals(""))
			return true;
		else
			return false;
	}

	public static String makeCiphers(int cipers, int digit) {
		StringBuilder index = new StringBuilder();
		index.append(digit);

		int temp = 0;
		int unit = 10;
		int count = 1;
		while ((temp = digit / unit) > 0) {
			unit *= 10;
			++count;
		}
		for (int j = 0; j < 5 - count; ++j) {
			index.insert(0, "0");
		}
		return index.toString();
	}

}
