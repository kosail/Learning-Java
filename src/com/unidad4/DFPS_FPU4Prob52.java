// Leer una matriz de 4x4 entera y determinar cuántas veces se repite en ella el número mayor.
package unidad4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DFPS_FPU4Prob52 {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int[][] numbers = new int[4][4];

		System.out.println("Ingresa 8 números enteros, uno a la vez:");
		for (int i=0;i<numbers.length;i++) {
			numbers[i] = Integer.parseInt(br.readLine());
		}

		br.close();
	}
}