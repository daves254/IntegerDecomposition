package dw.factor;

import java.util.function.Consumer;

public class Binary {
	private static final byte PWR2MAP[] = { -1, 0, 1, 39, 2, 15, 40, 23, 3, 12, 16, 59, 41, 19, 24, 54, 4, -1, 13, 10, 17, 62, 60, 28, 42, 30, 20, 51, 25, 44, 55, 47, 5, 32, 63, 38, 14, 22, 11, 58,
			18, 53, 63, 9, 61, 27, 29, 50, 43, 46, 31, 37, 21, 57, 52, 8, 26, 49, 45, 36, 56, 7, 48, 35, 6, 34, 33 };

	public static final byte find_one(long p) {
		return PWR2MAP[(int) ((((p & -p) % 67L) & 4095L) % 67)];
	}

	public static final byte find_one(long p, int after) {
		p &= ~((1L << after) - 1);
		return PWR2MAP[(int) ((((p & -p) % 67L) & 4095L) % 67)];
	}

	public static final byte find_zero(long p) {
		return find_one(~p);
	}

	public static final byte find_zero(long p, int after) {
		return find_one(~p, after);
	}

	public static final byte find_one(int p) {
		return find_one(int_to_long(p));
	}

	public static final byte find_one(int p, int after) {
		return find_one(int_to_long(p), after & 31);
	}

	public static final byte find_zero(int p) {
		return find_zero(int_to_long(p) | 0xffffffff00000000L);
	}

	public static final byte find_zero(int p, int after) {
		return find_zero(int_to_long(p) | 0xffffffff00000000L, after & 31);
	}

	public static final byte find_one(short p) {
		return find_one(short_to_long(p));
	}

	public static final byte find_one(short p, int after) {
		return find_one(short_to_long(p), after & 15);
	}

	public static final byte find_zero(short p) {
		return find_zero(short_to_long(p) | 0xffffffffffff0000L);
	}

	public static final byte find_zero(short p, int after) {
		return find_zero(short_to_long(p) | 0xffffffffffff0000L, after & 15);
	}

	public static final byte find_one(byte p) {
		return find_one(byte_to_long(p));
	}

	public static final byte find_one(byte p, int after) {
		return find_one(byte_to_long(p), after & 7);
	}

	public static final byte find_zero(byte p) {
		return find_zero(byte_to_long(p) | 0xffffffffffffff00L);
	}

	public static final byte find_zero(byte p, int after) {
		return find_zero(byte_to_long(p) | 0xffffffffffffff00L, after & 7);
	}

	public static final int byte_to_int(byte b) {
		return b & 0xFF;
	}

	public static final short byte_to_short(byte b) {
		return (short) (b & 0xFF);
	}

	public static final int short_to_int(short s) {
		return (s & 0xFFFF);
	}

	public static final long byte_to_long(byte b) {
		return b & 0xFF;
	}

	public static final long short_to_long(short s) {
		return s & 0xFFFF;
	}

	public static final long int_to_long(int i) {
		return i & 0xFFFFFFFFL;
	}

	public static final int to_reduced_int_size(int s) {
		if (s > 64) {
			int c = 0;
			while (s > ((1 << c) << 9))
				c++;
			return c;
		}
		return -1;
	}

	public static long trimWith1(long a, int rsize) {
		final long x = (1L << rsize) << 3L;
		return a & (((0x8000000000000000L ^ rsize) >> 63L) >>> (64L - x)) | (-(x >> rsize) << (x - 3));

	}

	public static long trimWith0(long a, int rsize) {
		final long x = (1L << rsize) << 3L;
		return a & (((0x8000000000000000L ^ rsize) >> 63L) >>> (64L - x));
	}

	public static int bitToByteSize(int i) {
		return (i >> 3) + (-(i & 7) >>> 31);
	}

	public static int bitToShortSize(int i) {
		return (i >> 4) + (-(i & 15) >>> 31);
	}

	public static int bitToIntSize(int i) {
		return (i >> 5) + (-(i & 31) >>> 31);
	}

	public static int bitToLongSize(int i) {
		return (i >> 6) + (-(i & 63) >>> 31);
	}

	public static void applyLower(int n, Consumer<int[]> c) {
		int[] args = new int[] { 0, 0, 0 };
		int y = 0;
		for (int i = 0; i < n; i++) {
			args[2] = y++;
			for (int j = i, k = 0; j >= 0; j--, k++) {
				args[0] = j;
				args[1] = k;
				c.accept(args);
			}
		}
	}

	public static void applyUpper(int n, Consumer<int[]> c) {
		int[] args = new int[] { 0, 0, 0 };
		int y = n;
		for (int i = 1; i < n; i++) {
			args[2] = y++;
			for (int j = n - 1, k = i; k < n; k++, j--) {
				args[0] = j;
				args[1] = k;
				c.accept(args);
			}
		}
	}

	public static void applyRowM(int n, Consumer<int[]> c) {
		int[] args = new int[] { 0, 0, 0 };
		int y = 0;
		for (int i = y; i < n; i++) {
			args[2] = y++;
			for (int j = i, k = 0; j >= 0; j--, k++) {
				args[0] = j;
				args[1] = k;
				c.accept(args);
			}
		}
		for (int i = y - n; i < n; i++) {
			args[2] = y++;
			for (int j = n - 1, k = i; k < n; k++, j--) {
				args[0] = j;
				args[1] = k;
				c.accept(args);
			}
		}
	}

	public static void applyRowAt(int n, int y, Consumer<int[]> c) {
		int[] args = new int[] { 0, 0, 0 };
		if (y < n) {
			int i = y;
			args[2] = y++;
			for (int j = i, k = 0; j >= 0; j--, k++) {
				args[0] = j;
				args[1] = k;
				c.accept(args);
			}
		} else
			for (int i = y - n; i < n;) {
				args[2] = y++;
				for (int j = n - 1, k = i; k < n; k++, j--) {
					args[0] = j;
					args[1] = k;
					c.accept(args);
				}
				break;
			}
	}

	public static int bitLength(int l) {
		return find_one(Integer.highestOneBit(l)) + 1;
	}

	public static void combineIntArray(int n, int r, Consumer<int[]> c) {
		combine1(n, 0, r, new int[r], c);
	}

	public static void combine1(int m, int n, int r, int work_space[], Consumer<int[]> c) {
		if (r == 0) {
			c.accept(work_space);
			return;
		}
		--r;
		for (int p = work_space.length - r - 1; n < m;) {
			work_space[p] = n++;
			combine1(m, n, r, work_space, c);
		}
	}

	public static void combine0(int n, int r, int work_space[], Consumer<int[]> c) {
		if (r == 0) {
			c.accept(work_space);
			return;
		}
		--r;
		for (int p = work_space.length - r - 1; n > 0;) {
			work_space[p] = --n;
			combine0(n, r, work_space, c);
		}
	}

	public static void combineShortArray(int n, int r, Consumer<short[]> c) {
		combine1(n, 0, r, new short[r], c);
	}

	public static void combine1(int m, int n, int r, short work_space[], Consumer<short[]> c) {
		if (r == 0) {
			c.accept(work_space);
			return;
		}
		--r;
		for (int p = work_space.length - r - 1; n < m;) {
			work_space[p] = (short) n++;
			combine1(m, n, r, work_space, c);
		}
	}

	public static void combine0(int n, int r, short work_space[], Consumer<short[]> c) {
		if (r == 0) {
			c.accept(work_space);
			return;
		}
		--r;
		for (int p = work_space.length - r - 1; n > 0;) {
			work_space[p] = (short) --n;
			combine0(n, r, work_space, c);
		}
	}
	
	/**
	 * Maximum Grid size
	 * @param colIndx
	 * @param bitLength
	 * @return
	 */
	public static int mgs_factor(int colIndx,int bitLength) {
		int r1 = (colIndx<<1)-bitLength;
		return colIndx+1-(r1 < 0 ? 0 : r1 + 1);
	}
	/**
	 * Maximum Foreign size
	 * @param colIndx
	 * @param bitLength
	 * @return
	 */
	public static int mfs_factor(int colIndx) {
		return Binary.bitLength(colIndx | 1) - 1;
	}

}
