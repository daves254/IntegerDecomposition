package dw.factor;

import java.util.function.Consumer;

public class ColMaxSize {

	public static void main(String[] args) {
		int m = 8;
		Binary.applyLower(m, new Consumer<int[]>() {

			@Override
			public void accept(int[] t) {
				int a = t[0];
				int b = t[1];
				int c = t[2];
				if (b == 0 || a == m - 1) {
					System.out.print((t[2]+1)+". SIZE["+(t[2]+1-i(a, c))+"]");
				}
				System.out.print("{I[P" + a + "=" + i(a, c) + ", Q" + b + "=" + i(b, c) + "] = " + qForm(a, b, c) + "} ");
				if (a == 0 || b == m - 1) {
					System.out.println();
				}
			}

			private int qForm(int a, int b, int c) {

				return i(a, c) + i(b, c);
			}

			private int i(int i, int cI) {
				int r = cI - (m - i);
				return r < 0 ? 0 : r + 1;
			}

		});

	}

}
