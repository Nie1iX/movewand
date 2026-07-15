# MoveWand Compatibility Matrix

Этот документ фиксирует границу поддержки для первого public alpha. Статус `planned test` означает: код не обещает совместимость, пока сценарий не пройден в реальном Fabric `1.21.1` мире с backup.

## Текущий contract

- MoveWand переносит `BlockState` и NBT `BlockEntity`; перед загрузкой NBT получает новые координаты `x`, `y`, `z`.
- Цель должна быть `air` либо позицией исходной группы, которая освобождается этой же операцией.
- Bedrock и блоки из `c:relocation_not_supported`, `forge:relocation_not_supported` или `create:non_movable` отклоняются до изменения мира. Неустановленные namespaces не создают dependency и просто не содержат блоков.
- Размер выделения ограничен 512 блоками, сдвиг pivot — 16 блоками.
- Совместимость сторонних модов не подразумевается только потому, что их блок смог пройти generic NBT path.

## Матрица

| Область | Статус | Что проверяем | Contract до проверки |
| --- | --- | --- | --- |
| Обычные vanilla blocks | code-covered | Сдвиг и поворот `BlockState`, включая `facing` и `axis` | Поддерживается |
| Chest, barrel, shulker box | planned test | Инвентарь, custom name, lock, поворот и повторное открытие | Не обещается |
| Furnace, smoker, blast furnace, brewing stand | planned test | Инвентарь и progress после сдвига | Не обещается |
| Hopper, dispenser, dropper | planned test | Инвентарь, redstone, взаимодействие с соседями после операции | Не обещается |
| Sign, lectern, banner, decorated pot | planned test | Текст, книга, pattern и custom data | Не обещается |
| Bedrock | code-covered | Выделение и применение операции | Всегда отклоняется |
| Блок из relocation denylist tags | code-covered | `c:`, `forge:` и Create data tags, попытка сдвига | Всегда отклоняется |
| Create ordinary blocks | planned integration test | Kinetic network, storage, smart blocks и поворот | Не обещается; нет зависимости от Create |
| AE2 | planned integration test | Кабели, machines, storage, network reconnect | Не обещается; нет зависимости от AE2 |

## Порядок проверки

1. Сделать backup test-world.
2. Для каждой vanilla-строки выполнить: сдвиг, поворот Y, повторный сдвиг, выход из мира и повторный вход.
3. Для Create проверить отдельный мир с работающим kinetic network до и после операции. Любой сбой переводит конкретный block id в denylist data pack до отдельной интеграции.
4. Для AE2 проверить отдельную простую ME network. Если сеть не восстанавливается, не применять generic NBT workaround: использовать API стратегии перемещения AE2 только как optional integration.
5. Проверить multiplayer: один игрок выполняет операцию, второй наблюдает итоговые blocks и inventories после reconnect.

## External contracts used as references

- Movable Block Entities использует `c:relocation_not_supported` как opt-out contract. MoveWand его учитывает.
- Create определяет `create:non_movable` и использует `forge:relocation_not_supported` для неподвижных блоков; MoveWand учитывает оба. `create:safe_nbt` относится к сохранению NBT при печати schematic, а не является общей гарантией безопасной relocation.
- AE2 имеет API strategy с `beginMove` / `completeMove`, в котором стратегия может отказаться от переноса. Его default path создаёт новый `BlockEntity` на target через `loadStatic`; это отличается от generic MoveWand path. MoveWand не добавляет AE2 как dependency до появления проверенной optional integration.
- WorldEdit перемещает selection только по явному `-s`; MoveWand всегда перемещает selection после успешной операции. Это намеренное UX-решение, поскольку жезл нужен для последовательной правки одной группы.
