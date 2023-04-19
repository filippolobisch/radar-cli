package helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PCPChooser {

	public ArrayList<String> choosePCPs() {
		try {
			System.out.println("Enter the integers you want to use for the PCPs:");
			System.out.println("\nPossible PCPs:");

			for (Field field : PCPTypes.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					System.out.printf("\t%s - type %d%n", field.getName(), field.get(null));
				}
			}

			Scanner scanner = new Scanner(System.in).useDelimiter("[,\\s+]");
			int num = scanner.nextInt();
			scanner.close();

			return PCPTypes.SENSITIVEDATAONCLOUD;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

//	private ArrayList<String> getRulesToInject(int index) {
//		if (index == PCPTypes.SENSITIVEDATAONCLOUD) {
//			return new ArrayList<String>(List.of("SensitiveDataOnCloud"));
//		} else {
//			return new ArrayList<String>(List.of("StoreDataOutsideEU"));
//		}
//	}
}
