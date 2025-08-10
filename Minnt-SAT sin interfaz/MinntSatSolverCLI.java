import java.util.*;
import java.io.*;

public class MinntSatSolverCLI {

  public static void main(String[] args) throws IOException {
    printLogo();
    System.out.println("Pega el contenido DIMACS (termina con una línea vacía):");

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    StringBuilder dimacsInput = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      if (line.trim().isEmpty()) break;
      dimacsInput.append(line).append("\n");
    }

    List<List<Integer>> clauses = parseDimacs(dimacsInput.toString());

    String[] headerParts = Arrays.stream(dimacsInput.toString().split("\n"))
            .filter(l -> l.startsWith("p"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Falta línea 'p cnf'"))
            .split("\\s+");
    int numVars = Integer.parseInt(headerParts[2]);

    // Validar que todas las variables estén en rango
    for (List<Integer> clause : clauses) {
      for (int lit : clause) {
        int var = Math.abs(lit);
        if (var < 1 || var > numVars) {
          throw new RuntimeException("Literal " + lit + " fuera de rango. numVars=" + numVars);
        }
      }
    }

    Boolean[] solution = solveSAT(clauses, numVars);

    if (solution != null) {
      List<Integer> dimacsAssign = new ArrayList<>();
      for (int i = 0; i < solution.length; i++) {
        dimacsAssign.add(solution[i] ? (i + 1) : -(i + 1));
      }
      System.out.println("SAT");
      System.out.println("Asignación (formato DIMACS): " + dimacsAssign);
      System.out.println("Valores de variables:");
      for (int i = 0; i < solution.length; i++) {
        System.out.println((i + 1) + " = " + (solution[i] ? "True" : "False"));
      }
    } else {
      System.out.println("UNSAT");
    }
  }

  static List<List<Integer>> parseDimacs(String dimacsStr) {
    List<List<Integer>> clauses = new ArrayList<>();
    String[] lines = dimacsStr.split("\n");
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("c") || line.startsWith("p") || line.startsWith("%")) {
        continue;
      }

      List<Integer> clause = new ArrayList<>();
      String[] parts = line.split("\\s+");
      for (String p : parts) {
        if (p.equals("0")) {
          break;
        }
        if (!p.isEmpty()) {
          try {
            clause.add(Integer.parseInt(p));
          } catch (NumberFormatException e) {
            // Ignorar tokens no numéricos
          }
        }
      }
      clauses.add(clause);
    }
    return clauses;
  }

  static class DPLLContext {
    Boolean[] assignment;
    List<List<Integer>> clauses;
    int numVars;

    DPLLContext(int numVars, List<List<Integer>> clauses) {
      this.numVars = numVars;
      this.clauses = clauses;
      this.assignment = new Boolean[numVars];
    }

    public DPLLContext clone() {
      DPLLContext copy = new DPLLContext(this.numVars, this.clauses);
      copy.assignment = this.assignment.clone();
      return copy;
    }
  }

  static Boolean[] solveSAT(List<List<Integer>> clauses, int numVars) {
    DPLLContext context = new DPLLContext(numVars, clauses);
    return dpll(context);
  }

  static Boolean[] dpll(DPLLContext context) {
    while (true) {
      boolean changed = false;

      // 1. Propagación unitaria
      Integer unitLiteral = findUnitClause(context);
      if (unitLiteral != null) {
        assignLiteral(unitLiteral, context);
        changed = true;
      }

      // 2. Eliminación de literales puros
      List<Integer> pureLiterals = findPureLiterals(context);
      if (!pureLiterals.isEmpty()) {
        for (int lit : pureLiterals) {
          assignLiteral(lit, context);
        }
        changed = true;
      }

      // Verificar estado actual
      int status = checkClausesStatus(context);
      if (status == 1) {  // SAT
        return context.assignment;
      } else if (status == -1) {  // UNSAT
        return null;
      }

      if (!changed) {
        break;
      }
    }

    // Seleccionar variable no asignada
    int var = selectUnassignedVariable(context);
    if (var == -1) {
      return null;  // No debería ocurrir
    }

    // Probar asignación TRUE
    DPLLContext contextTrue = context.clone();
    contextTrue.assignment[var] = true;
    Boolean[] result = dpll(contextTrue);
    if (result != null) return result;

    // Probar asignación FALSE
    DPLLContext contextFalse = context.clone();
    contextFalse.assignment[var] = false;
    result = dpll(contextFalse);
    if (result != null) return result;

    return null;
  }

  static Integer findUnitClause(DPLLContext context) {
    for (List<Integer> clause : context.clauses) {
      int unassignedCount = 0;
      Integer unassignedLiteral = null;
      boolean isSatisfied = false;

      for (int lit : clause) {
        Boolean value = evaluateLiteral(lit, context.assignment);
        if (value == null) {
          unassignedCount++;
          unassignedLiteral = lit;
        } else if (value) {
          isSatisfied = true;
          break;
        }
      }

      if (!isSatisfied && unassignedCount == 1) {
        return unassignedLiteral;
      }
    }
    return null;
  }

  static List<Integer> findPureLiterals(DPLLContext context) {
    int[] signs = new int[context.numVars]; // 0: no visto, 1: solo positivo, -1: solo negativo, 2: mixto
    List<Integer> pureLiterals = new ArrayList<>();

    for (List<Integer> clause : context.clauses) {
      if (isClauseSatisfied(clause, context.assignment)) continue;

      Set<Integer> seenVars = new HashSet<>();
      for (int lit : clause) {
        int var = Math.abs(lit) - 1;
        if (seenVars.contains(var)) continue;
        seenVars.add(var);

        if (context.assignment[var] != null) continue;

        int sign = (lit > 0) ? 1 : -1;
        if (signs[var] == 0) {
          signs[var] = sign;
        } else if (signs[var] != sign) {
          signs[var] = 2;
        }
      }
    }

    for (int i = 0; i < signs.length; i++) {
      if (context.assignment[i] == null && signs[i] != 0 && signs[i] != 2) {
        pureLiterals.add(signs[i] > 0 ? (i + 1) : -(i + 1));
      }
    }

    return pureLiterals;
  }

  static void assignLiteral(int literal, DPLLContext context) {
    int var = Math.abs(literal) - 1;
    if (var >= context.assignment.length) {
      throw new RuntimeException("Variable no declarada: " + Math.abs(literal));
    }
    context.assignment[var] = (literal > 0);
  }

  static Boolean evaluateLiteral(int lit, Boolean[] assignment) {
    int var = Math.abs(lit) - 1;
    if (var >= assignment.length || assignment[var] == null) {
      return null;
    }
    return (lit > 0) ? assignment[var] : !assignment[var];
  }

  static int checkClausesStatus(DPLLContext context) {
    boolean allSatisfied = true;

    for (List<Integer> clause : context.clauses) {
      boolean hasUnassigned = false;
      boolean clauseSatisfied = false;

      for (int lit : clause) {
        Boolean value = evaluateLiteral(lit, context.assignment);
        if (value == null) {
          hasUnassigned = true;
        } else if (value) {
          clauseSatisfied = true;
          break;
        }
      }

      if (!clauseSatisfied) {
        if (!hasUnassigned) {
          return -1; // UNSAT
        }
        allSatisfied = false;
      }
    }

    return allSatisfied ? 1 : 0; // 1: SAT, 0: Indeterminado
  }

  static boolean isClauseSatisfied(List<Integer> clause, Boolean[] assignment) {
    for (int lit : clause) {
      Boolean value = evaluateLiteral(lit, assignment);
      if (value != null && value) {
        return true;
      }
    }
    return false;
  }

  static int selectUnassignedVariable(DPLLContext context) {

    for (int i = 0; i < context.assignment.length; i++) {
      if (context.assignment[i] == null) {
        return i;
      }
    }
    return -1;
  }

  static void printLogo() {
    System.out.println("""
 __  __  _              _   
|  \\/  |(_) _ _   _ _  | |_ 
| |\\/| || || ' \\ | ' \\ |  _|
|_|  |_||_||_||_||_||_| \\__|
 ___        _    ___       _                
/ __| __ _ | |_ / __| ___ | |__ __ ___  _ _ 
\\__ \\/ _` ||  _|\\__ \\/ _ \\| |\\ V // -_)| '_|
|___/\\__/_| \\__||___/\\___/|_| \\_/ \\___||_|  
""");
  }
}
