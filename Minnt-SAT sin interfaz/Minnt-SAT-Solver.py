print(r"""
 __  __  _              _   
|  \/  |(_) _ _   _ _  | |_ 
| |\/| || || ' \ | ' \ |  _|
|_|  |_||_||_||_||_||_| \__|
 ___        _    ___       _                
/ __| __ _ | |_ / __| ___ | |__ __ ___  _ _ 
\__ \/ _` ||  _|\__ \/ _ \| |\ V // -_)| '_|
|___/\__/_| \__||___/\___/|_| \_/ \___||_|  
""")


def parse_dimacs(dimacs_str):
    lines = dimacs_str.strip().split("\n")
    clauses = []
    for line in lines:
        line = line.strip()
        if line.startswith("c") or line.startswith("p"):
            continue
        clause = [int(x) for x in line.split() if x != "0"]
        if clause:
            clauses.append(clause)
    return clauses

def is_satisfied(clauses, assignment):
    for clause in clauses:
        if not any((lit > 0 and assignment[abs(lit)-1]) or (lit < 0 and not assignment[abs(lit)-1]) for lit in clause):
            return False
    return True

def solve_sat(clauses, num_vars):
    def backtrack(assignment):
        if len(assignment) == num_vars:
            return assignment if is_satisfied(clauses, assignment) else None
        for value in [True, False]:
            result = backtrack(assignment + [value])
            if result is not None:
                return result
        return None
    return backtrack([])

print("Pega el contenido DIMACS (termina con una línea vacía):")
dimacs_input = ""
while True:
    line = input()
    if line.strip() == "":
        break
    dimacs_input += line + "\n"

clauses = parse_dimacs(dimacs_input)
header = [line for line in dimacs_input.split("\n") if line.startswith("p")][0].split()
num_vars = int(header[2])

solution = solve_sat(clauses, num_vars)

if solution:
    dimacs_assign = []
    for i, val in enumerate(solution, start=1):
        dimacs_assign.append(i if val else -i)
    
    print("SAT")
    print("Asignación (formato DIMACS):", dimacs_assign)
    
    print("Valores de variables:")
    for i, val in enumerate(solution, start=1):
        print(f"{i} = {'True' if val else 'False'}")
else:
    print("UNSAT")