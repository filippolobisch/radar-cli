package helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// https://stackoverflow.com/questions/36455239/get-all-static-string-values-from-a-final-class-as-a-string-array-or-list

public class PCPChooser {

	public List<Integer> choosePCPs() {
		List<Integer> nums = new ArrayList<Integer>();

		try {
			System.out.println(
					"Enter the integers you want to use for the PCPs: (Maximum of 3 PCPs indexes or index 1 and 2 to use default and all) ");
			System.out.println("\nPossible PCPs:");

			List<Field> fields = new ArrayList<Field>();
			for (Field field : PCPTypes.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					fields.add(field);
					System.out.println(String.format("\t%s - type %d", field.getName(), field.get(null)));
				}
			}

			Scanner scanner = new Scanner(System.in).useDelimiter("[,\\s+]");

			for (int i = 0; i < fields.size(); i++) {
				if (scanner.hasNextInt()) {
					int num = scanner.nextInt();
					nums.add(num);

					if (num == 0 || num == 1) {
						nums = null;
						nums = new ArrayList<Integer>();
						nums.add(num);
						break;
					}
				} else {
					break;
				}
			}

			scanner.close();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return nums;
	}

	public String[] pcpToUse(List<Integer> indexesPCP) {
		String[] pcps = new String[indexesPCP.size()];
		int addIndex = 0;
		for (int index : indexesPCP) {
			if (index == 0 || index == 1) {
				return getRulesToInject(index);
			} else {
				pcps[addIndex] = getRulesToInject(index)[0];
				addIndex += 1;
			}
		}
		return pcps;
	}

	public String[] getRulesToInject(int index) {
		switch (index) {
			case PCPTypes.ALL:
				String[] allPCPs = { "CreateUnauthorizedAccess", "StoreDataOutsideEU", "StoreDataLocally",
						"SensitiveDataOnCloud" };
				return allPCPs;
			case PCPTypes.CREATEUNAUTHORIZESACCESS:
				String[] unauthAccessPcp = { "CreateUnauthorizedAccess" };
				return unauthAccessPcp;
			case PCPTypes.STOREDATAOUTSIDEEU:
				String[] storeDataOutsideEUPcp = { "StoreDataOutsideEU" };
				return storeDataOutsideEUPcp;
			case PCPTypes.STOREDATALOCALLY:
				String[] storeDataLocallyPcp = { "StoreDataLocally" };
				return storeDataLocallyPcp;
			case PCPTypes.SENSITIVEDATAONCLOUD:
				String[] sensitiveDataOnCloud = { "SensitiveDataOnCloud" };
				return sensitiveDataOnCloud;
			case PCPTypes.DEFAULT:
			default:
				String[] defaultPCP = { "CreateUnauthorizedAccess", "StoreDataOutsideEU" };
				return defaultPCP;
		}
	}
}
