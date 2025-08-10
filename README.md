[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# Minnt SAT Solver

Minnt SAT Solver es un solucionador SAT basado en Java que implementa el algoritmo DPLL con una interfaz gráfica intuitiva. Soporta archivos CNF en formato DIMACS y ofrece visualización detallada de resultados.

## Características principales

- Implementación del algoritmo DPLL con optimizaciones:
  - Propagación de unidades
  - Eliminación de literales puros
  - Heurística de selección de variables
- Interfaz gráfica con editor de texto y panel de resultados
- Soporte para temas claros y oscuros
- Procesamiento eficiente con capacidad de cancelación
- Soporte completo para formato DIMACS CNF

## Requisitos

- Java JDK 17 o superior
- Sistemas compatibles: Windows, Linux, macOS

## Instalación

1. Clonar el repositorio:
   git clone [https://github.com/tuusuario/minnt-sat-solver](https://github.com/Minnt-D/Minnt-SAT-solver).git
cd minnt-sat-solver
2. Compilar con Maven:
3. Ejecutar:
   java -jar target/minnt-sat-solver-1.0.jar


## Uso básico

1. Ingresar fórmula CNF en el editor o cargar desde archivo
2. Hacer clic en "Resolver" o usar Ctrl+R
3. Ver resultados en el panel derecho:
   - Estado SAT/UNSAT
   - Asignación de variables
   - Tiempo de procesamiento

## Ejemplo de entrada

p cnf 3 2
1 -3 0
2 3 -1 0


## Licencia

Este proyecto está licenciado bajo la GNU General Public License (GPL). Ver el archivo LICENSE para más detalles.

## Contribuciones

Se aceptan contribuciones mediante pull requests. Por favor reportar issues para cualquier problema o sugerencia.
