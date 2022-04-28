package dw.factor;

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

import dw.factor.GLPKOptimizorFactorizor.GLPKHandler.Msg;

public class GLPKOptimizorFactorizor {
	public static class GLPKHandler {
		enum Msg {
			OFF(GLPKConstants.GLP_MSG_OFF, GLPKConstants.GLP_JAVA_MSG_LVL_OFF), //
			ON(GLPKConstants.GLP_MSG_ON, GLPKConstants.GLP_JAVA_MSG_LVL_ALL), //
			DEBUG(GLPKConstants.GLP_MSG_DBG, GLPKConstants.GLP_JAVA_MSG_LVL_ALL), //
			ERROR(GLPKConstants.GLP_MSG_ERR, GLPKConstants.GLP_JAVA_MSG_LVL_OFF), //
			ALL(GLPKConstants.GLP_MSG_ALL, GLPKConstants.GLP_JAVA_MSG_LVL_ALL);

			private final int glpkMsgFlag;
			private final int javaMsgFlag;

			Msg(int msgFlag, int jmsgFlag) {
				this.glpkMsgFlag = msgFlag;
				this.javaMsgFlag = jmsgFlag;
			}

			public int toGLPKFlag() {
				return glpkMsgFlag;
			}
			public int toJavaFlag() {
				return javaMsgFlag;
			}
		}

		private int next_col_pos = 1;
		private int next_row_pos = 1;
		private glp_prob prob;
		private boolean objective_opened;
		private glp_iocp parm;
		private int ret;
		private boolean solved;
		private boolean max;

		public GLPKHandler(String name, boolean max) {
			this.prob = GLPK.glp_create_prob();
			GLPK.glp_set_prob_name(prob, name);
			this.max = max;
			ensureIOCP();
		}

		public GLPKHandler(String name) {
			this(name, true);
		}

		int useNextColumnPos() {
			check();
			return next_col_pos++;
		}

		public void addVarSize(int n) {
			check();
			GLPK.glp_add_cols(prob, n);
		}

		public int registeredVarCount() {
			check();
			return next_col_pos - 1;
		}

		public void openObjective() {
			check();
			if (objective_opened)
				return;
			GLPK.glp_set_obj_dir(prob, max ? GLPKConstants.GLP_MAX : GLPKConstants.GLP_MIN);
			objective_opened = true;
		}

		public int useNextRow() {
			check();
			return next_row_pos++;
		}

		public void addEquationSize(int n) {
			check();
			GLPK.glp_add_rows(prob, n);
		}

		public boolean solve() {
			check();
			int ret = GLPK.glp_intopt(prob, parm);
			this.ret = ret;
			if (ret == 0) {
				solved = true;
				return true;
			} else {
				solved = false;
				return false;
			}
		}

		private glp_iocp ensureIOCP() {
			check();
			if (parm == null) {
				parm = new glp_iocp();
				GLPK.glp_init_iocp(parm);
				parm.setPresolve(GLPKConstants.GLP_ON);
			}
			return parm;
		}

		private void check() {
			if (prob == null)
				throw new IllegalStateException();
		}

		public String message() {
			if (ret == 0) {
				return "The LP problem instance has been successfully solved. "//
						+ "(This code does not necessarily mean that the solver has "//
						+ "found optimal solution. It only means that the solution "//
						+ "process was successful.)";
			} else if (ret == GLPKConstants.GLP_EROOT) {
				return "Unable to start the search, because optimal basis for initial "//
						+ "LP relaxation is not provided. (This code may appear only "//
						+ "if the presolver is disabled.)";
			} else if (ret == GLPKConstants.GLP_EMIPGAP) {
				return "The search was prematurely terminated, because the rela"//
						+ "tive mip gap tolerance has been reached.";
			} else if (ret == GLPKConstants.GLP_ESTOP) {
				return "The search was prematurely terminated by application. "//
						+ "(This code may appear only if the advanced solver inter"//
						+ "face is used.)";
			}

			else if (ret == GLPKConstants.GLP_EBADB) {
				return "Unable to start the search, because the initial basis speci"//
						+ "fied in the problem object is invalid—the number of basic "//
						+ "(auxiliary and structural) variables is not the same as the "//
						+ "number of rows in the problem object.";
			} else if (ret == GLPKConstants.GLP_ESING) {
				return "Unable to start the search, because the basis matrix corre"//
						+ "sponding to the initial basis is singular within the working"//
						+ "precision.";
			} else if (ret == GLPKConstants.GLP_ECOND) {
				return "Unable to start the search, because the basis matrix cor"//
						+ "responding to the initial basis is ill-conditioned, i.e. its "//
						+ "condition number is too large";
			} else if (ret == GLPKConstants.GLP_EBOUND) {
				return "Unable to start the search, because some double-bounded "//
						+ "(auxiliary or structural) variables have incorrect bounds.";
			} else if (ret == GLPKConstants.GLP_EFAIL) {
				return "The search was prematurely terminated due to the solver "//
						+ "failure.";
			} else if (ret == GLPKConstants.GLP_EOBJLL) {
				return "The search was prematurely terminated, because the ob"//
						+ "jective function being maximized has reached its lower "//
						+ "limit and continues decreasing (the dual simplex only).";
			} else if (ret == GLPKConstants.GLP_EOBJUL) {
				return "The search was prematurely terminated, because the ob"//
						+ "jective function being minimized has reached its upper"//
						+ "limit and continues increasing (the dual simplex only).";
			} else if (ret == GLPKConstants.GLP_EITLIM) {
				return "The search was prematurely terminated, because the sim"//
						+ "plex iteration limit has been exceeded.";
			} else if (ret == GLPKConstants.GLP_ETMLIM) {
				return "The search was prematurely terminated, because the time"//
						+ "limit has been exceeded.";
			} else if (ret == GLPKConstants.GLP_ENOPFS) {
				return "The LP problem instance has no primal feasible solution"//
						+ "(only if the LP presolver is used).";
			} else if (ret == GLPKConstants.GLP_ENODFS) {
				return "The LP problem instance has no dual feasible solution"//
						+ "(only if the LP presolver is used).";
			}

			else {
				return "System could not be solved. Unkown reason 0x" + Integer.toHexString(ret);
			}
		}

		public double getObjective() {
			check();
			checkSolutions();
			return GLPK.glp_mip_obj_val(prob);
		}

		private void checkSolutions() {
			if (!solved)
				throw new IllegalStateException();
		}

		public void setMsgLevel(Msg msg) { 
			ensureIOCP().setMsg_lev(msg.toGLPKFlag());
			GLPK.glp_java_set_msg_lvl(msg.toJavaFlag());
		}

		public void delete() {
			if (prob != null) {
				prob.delete();
				prob = null;
				if (parm != null)
					parm.delete();
				parm = null;
			}
		}

	}

	static class Symbol {
		static int sysms;
		final int origin;
		final int current;
		private int ordinal;
		private short value = -1;
		private short max = -1;

		public Symbol(int origin, int current) {
			this.origin = origin;
			this.current = current;
		}

		public boolean isPrimaryVariable() {
			return origin == current;
		}

		public void asGLPKVar(GLPKHandler param) {
			int ordinal = this.ordinal;
			if (ordinal == 0)
				ordinal = this.ordinal = param.useNextColumnPos();
			glp_prob prob = param.prob;
			GLPK.glp_set_col_name(prob, ordinal, toIdString());
			int kind;
			if (isPrimaryVariable()) {
				kind = GLPKConstants.GLP_IV;
				GLPK.glp_set_col_bnds(prob, ordinal, GLPKConstants.GLP_DB, 0, max);
			} else {
				kind = GLPKConstants.GLP_BV;
			}
			GLPK.glp_set_col_kind(prob, ordinal, kind);
			param.openObjective();
			if (isPrimaryVariable())
				GLPK.glp_set_obj_coef(prob, ordinal, 1);
		}

		private String toIdString() {
			return ("s" + getId()).intern();
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			stringForm(str);
			return str.toString();
		}

		private void stringForm(StringBuilder str) {
			int b = 1 << (current - origin);
			if (b == 1) {
				str.append("M");
			} else
				str.append("'").append(b).append("C");
			str.append(origin);
		}

		public boolean isSolved() {
			return value >= 0;
		}

		public double getValue(GLPKHandler param) {
			param.checkSolutions();
			int k = ordinal;
			if (k != 0)
				return GLPK.glp_mip_col_val(param.prob, k);
			else
				throw new IllegalStateException();
		}

		private int getOrdinal() {
			return ordinal;
		}

		public int getId() {
			return (origin << 16) | (current);
		}

		public void setMax(int max) {
			this.max = (short) max;
		}

	}

	static class Term {
		Symbol sym;
		int coefficient = 1;

		public Term(Symbol sym) {
			this.sym = sym;
		}

		public Term(Symbol sym, int coeff) {
			super();
			this.sym = sym;
			this.coefficient = coeff;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			stringForm(str, "", "-");
			return str.toString();
		}

		private void stringForm(StringBuilder str, String ps, String ns) {
			int c = Math.abs(coefficient);
			if (coefficient < 0) {
				str.append(ns);
			} else
				str.append(ps);
			if (c != 1) {
				str.append(c).append("*");
			}
			sym.stringForm(str);

		}

		public double getValue(GLPKHandler handler) {
			return coefficient * sym.getValue(handler);
		}

	}

	static class EQ {
		boolean value;
		int pos;
		Term[] terms;
		int m = 0;
		int n = 0;
		private int id;

		public EQ(int pos, int n) {
			this.pos = pos;
			this.n = alpha(pos);
			int beta = Math.min(n - pos, this.n);
			this.terms = new Term[this.n + beta];
		}

		public void forAllAuxVars(Consumer<Symbol> consumer) {
			for (int i = EQ.alpha(pos); i < n; i++) {
				consumer.accept(terms[i].sym);
			}

		}

		private static int alpha(int pos) {
			return Binary.bitLength(pos) + 1 + ((-pos) >> 31);
		}

		void add(Term t) {
			terms[m++] = t;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < m; i++) {
				appendTermToStringBuilder(sb, i);
			}
			for (int i = alpha(pos); i < n; i++) {
				appendTermToStringBuilder(sb, i);
			}
			return sb.toString();
		}

		private void appendTermToStringBuilder(StringBuilder sb, int at) {
			Term term = terms[at];

			{
				String ps = "", ns = "-";
				if (!sb.isEmpty()) {
					ps = " + ";
					ns = " - ";
				}
				if (term != null)
					term.stringForm(sb, ps, ns);
				else
					sb.append(ps).append("null");
			}
		}

		public int getCarriageRange() {
			return Binary.bitLength(pos + 1 + m);
		}

		private void addBeta(Term t) {
			terms[n++] = t;
		}

		public void recordAuxilliaryVariables(GLPKHandler param) {
			for (int i = EQ.alpha(pos); i < n; i++) {
				terms[i].sym.asGLPKVar(param);
			}
		}

		public void recordPrimaryVariable(GLPKHandler param) {
			Symbol sym = terms[m - 1].sym;
			if (!sym.isSolved()) {
				sym.asGLPKVar(param);
			}

		}

		public void applyEquation(GLPKHandler param) {
			if (id == 0) {
				int alpha = alpha(pos);
				int d = m + (n - alpha) + 1;
				SWIGTYPE_p_int iar = GLPK.new_intArray(d);
				SWIGTYPE_p_double dar = GLPK.new_doubleArray(d);
				int j = 1;
				for (int i = 0; i < m; i++, j++) {
					Term term = terms[i];
					GLPK.intArray_setitem(iar, j, term.sym.getOrdinal());
					GLPK.doubleArray_setitem(dar, j, term.coefficient);
				}
				for (int i = alpha; i < n; i++, j++) {
					Term term = terms[i];
					GLPK.intArray_setitem(iar, j, term.sym.getOrdinal());
					GLPK.doubleArray_setitem(dar, j, term.coefficient);
				}
				int v = value ? 1 : 0;
				int id = this.id = param.useNextRow();
				GLPK.glp_set_row_name(param.prob, id, strId());
				GLPK.glp_set_row_bnds(param.prob, id, GLPKConstants.GLP_FX, v, v);
				GLPK.glp_set_mat_row(param.prob, id, d - 1, iar, dar);
				GLPK.delete_doubleArray(dar);
				GLPK.delete_intArray(iar);
			} else
				throw new IllegalStateException();
		}

		private String strId() {
			return "r" + pos;
		}

		public boolean isConformant(GLPKHandler handler) {
			double r = 0;
			for (int i = 0; i < m; i++) {
				r += terms[i].getValue(handler);
			}
			for (int i = alpha(pos); i < n; i++) {
				r += terms[i].getValue(handler);
			}
			return Double.valueOf(r).equals(Double.valueOf(1.0)) == value;
		}

	}

	static class Constraints {
		Hashtable<Symbol, Term> t;
	}
	public static BigInteger generate(Random rnd, int b, BigInteger p) {
		BigInteger q = BigInteger.probablePrime(b, rnd);
		while (q.equals(p))
			q = generate(rnd, b, p);
		return q;
	}
	public static void main(String... args) {

		Random rnd = new Random();
		int n = 12;
		BigInteger p = generate(rnd, n, BigInteger.ONE);
		BigInteger q = generate(rnd, n, p);
		BigInteger N = p.multiply(q);
		factorize(N);
	}

	private static void factorize(BigInteger N) {
		TreeMap<Integer, Symbol> syms = new TreeMap<>();
		int n = N.bitLength();
		EQ[] eqs = new EQ[n];
		for (int i = 0; i < n; i++) {
			EQ eq = new EQ(i, n);
			eqs[i] = eq;
			eq.value = N.testBit(i);
		}
		int bvars = 0;
		int mvars = 0;
		for (int i = 0; i < n; i++) {
			EQ e = eqs[i];
			mvars++;
			for (int j = 0, m = Math.min(n - i, e.getCarriageRange()); j < m; j++) {
				int current = j + i;
				Symbol sym = new Symbol(i, current);
				syms.put(sym.getId(), sym);
				Term t1 = new Term(sym);
				eqs[current].add(t1);
				if (j != 0) {
					Term t2 = new Term(sym, -(1 << (current - i)));
					bvars++;
					e.addBeta(t2);
				} else
					sym.setMax(i + 1);

			}
			
		}
		solve(eqs, bvars, mvars);
	}
	private static void solve(EQ[] eqs, int bvars, int mvars) {
		GLPKHandler handler = new GLPKHandler("Factorization-" + System.currentTimeMillis());
		//handler.setMsgLevel(Msg.OFF);
		handler.addVarSize(bvars + mvars);
		handler.addEquationSize(eqs.length);
		for (EQ e : eqs) {
			e.recordPrimaryVariable(handler);
		}
		handler.addEquationSize(eqs.length);
		for (EQ e : eqs) {
			e.recordAuxilliaryVariables(handler);
			e.applyEquation(handler);
			//System.out.println((e.value ? 1 : 0) + " = " + e.toString());
		}
		// Add Constraints

		// We control messages to be printed from here
		if (handler.solve()) {
			System.out.println("OBJECTIVE: " + handler.getObjective());
//			for (Symbol sym : syms.values()) {
//				System.out.println(sym.toString() + " = " + sym.getValue(handler));
//			}
			boolean b = true;
			for (EQ e : eqs) {
				b &= e.isConformant(handler);
			}
			System.out.println("PARANOIA CHECK " + (b ? "PASSED" : "FAILED"));
		} else
			System.out.println(handler.message());
		System.out.println("Boolean Vars: " + bvars);
		System.out.println("Non-Boolean Vars: " + mvars);
		System.out.println("Registered Vars: " + handler.registeredVarCount());
		handler.delete();
	}
}
