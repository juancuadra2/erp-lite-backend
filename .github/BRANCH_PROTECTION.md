# Branch Protection Rules

Para asegurar que los PRs cumplan con los requisitos de cobertura de código antes de ser mergeados a `main`, es necesario configurar las reglas de protección de rama en GitHub.

## Configuración Requerida

1. Ve a **Settings** > **Branches** en el repositorio de GitHub
2. Agrega una regla de protección para la rama `main`
3. Habilita las siguientes opciones:

### Opciones Requeridas:

- ✅ **Require a pull request before merging**
  - ✅ Require approvals: 1 (opcional, pero recomendado)
  
- ✅ **Require status checks to pass before merging**
  - ✅ Require branches to be up to date before merging
  - Buscar y seleccionar el status check: **coverage**
  
- ✅ **Do not allow bypassing the above settings** (opcional pero recomendado)

## ¿Qué hace esto?

Una vez configurado, el workflow `Code Coverage Validation` se ejecutará automáticamente en cada Pull Request a `main` y verificará:

1. **Cobertura Total ≥ 90%**: La cobertura total del proyecto debe ser al menos 90%
2. **Cobertura de Archivos Nuevos/Modificados = 100%**: Todos los archivos Java nuevos o modificados deben tener 100% de cobertura

Si cualquiera de estas validaciones falla, el PR no podrá ser mergeado hasta que se corrija.

## Verificación

Para verificar que la configuración está correcta:

1. Crea un PR de prueba con cambios en un archivo Java
2. Verifica que el workflow "Code Coverage Validation" se ejecute automáticamente
3. Intenta mergear el PR antes de que el workflow termine (debería estar bloqueado)
4. Una vez que el workflow termine, verifica que solo puedas mergear si las validaciones pasan
