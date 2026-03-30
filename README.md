# EMA-Kotlin

`EMA-Kotlin` is a Kotlin/JVM implementation of the **Expressive and Modular Activation (EMA)** mechanism for Context-Oriented Programming (COP).
This implementation is inspired by and extends the ideas presented in the EMA paper and EMA-js reference implementation.

## Status

- Core runtime implemented in Kotlin (`Signal`, `SignalComp`, `Layer`, `EMA`, partial methods, `proceed`).
- EMA-js behavior ported to Kotlin tests.
- Public playground included under `docs/` with multiple Kotlin examples.
- GitHub Pages workflow included for automatic playground deployment.

## Highlights

- **Signal-driven activation** with declarative condition expressions.
- **Higher-order and recursive signal compositions**.
- **Dynamic layer deployment / undeployment**.
- **Partial method adaptation** with `Layer.proceed(...)`.
- **Layer enter/exit hooks** and adaptation conflict scenarios.

## Build & Test

```bash
./gradlew test --no-daemon
```

Current suite:

- **82 tests**
- Includes Kotlin ports of EMA-js tests + additional coverage tests

## NPM Package

- Package: [`ema-kotlin`](https://www.npmjs.com/package/ema-kotlin)
- Repository: <https://github.com/pleger/EMA-Kotlin>

## Citation (APA)

Leger, P., Cardozo, N., & Masuhara, H. (2023). An expressive and modular layer activation mechanism for Context-Oriented Programming. *Information and Software Technology, 156*, 107132. https://doi.org/10.1016/j.infsof.2022.107132

## Quick Start

```kotlin
import org.emakotlin.*

fun main() {
    EMA.init()

    val player = DynamicObject().apply {
        set("energy", Signal(0))
        defineMethod("speed") { 10 }
    }

    val turboLayer = LayerSpec(
        condition = SignalComp("e > 30")
    )

    EMA.exhibit(player, mapOf("e" to player.get<Signal>("energy")))
    EMA.addPartialMethod(turboLayer, player, "speed") {
        val base = Layer.proceed() as Int
        base + 7
    }

    println(player.call("speed")) // 10
    EMA.deploy(turboLayer)

    player.get<Signal>("energy").mutableValue = 40
    println(player.call("speed")) // 17
}
```

## Core API

- `Signal(initialValue, id = "_")`
- `SignalComp(expression, signals = emptyList(), id = "_")`
- `LayerSpec(condition, enter, exit, name)`
- `EMA.deploy(layerSpec)` / `EMA.undeploy(layerSpec)`
- `EMA.exhibit(target, mapOf("id" to signal))`
- `EMA.addPartialMethod(layerSpec, targetObj|listOfObjs, methodName) { ... }`
- `Layer.proceed(vararg args)`

## Playground

- Static site: [`docs/index.html`](docs/index.html)
- Deploys through: [`.github/workflows/pages.yml`](.github/workflows/pages.yml)
- Intended public URL (after enabling Pages):
  - `https://pleger.github.io/EMA-Kotlin/`

Playground examples included:

1. Signal composition
2. Enter/exit transitions
3. Partial method + proceed
4. Recursive context state

## Project Structure

- `src/main/kotlin/org/emakotlin/`: EMA-Kotlin runtime
- `src/test/kotlin/org/emakotlin/`: translated EMA-js tests + extra tests
- `docs/`: playground and examples for GitHub Pages

## Compatibility Notes

- Designed for Kotlin/JVM.
- Uses Gradle wrapper and JDK 21 toolchain defaults in `gradle.properties`.

## Related

- EMA-js reference implementation: <https://github.com/pragmaticslaboratory/EMAjs>
- AspectScript playground inspiration: <https://pleger.github.io/aspectscript>

## License

MIT (see [LICENSE](LICENSE)).
