# MoveWand Functional Specification

## Selection

- `SELECT_BOX_START` / `SELECT_BOX_END` — define a rectangular selection with two corners.
- `SELECT_BLOCK_ADD` / `SELECT_BLOCK_REMOVE` — change an individual-block selection.
- `CLEAR_SELECTION` — clear all selected blocks and selection feedback.
- `SHOW_SELECTION_OUTLINE` — render the selected blocks and a pending first box corner while MoveWand is in the main hand.

Beginning an individual-block selection clears a pending first box corner. Doors and beds expand the selection to include their matching half. A double chest is valid only when both halves are selected.

## Transformation

- `MOVE_FORWARD` / `MOVE_BACKWARD`
- `MOVE_LEFT` / `MOVE_RIGHT`
- `MOVE_UP` / `MOVE_DOWN`
- `ROTATE_Y_CLOCKWISE` / `ROTATE_Y_COUNTERCLOCKWISE`
- `SHOW_TRANSFORM_PREVIEW` — render the projected structure before applying it.
- `APPLY_TRANSFORM` — request a validated transform from the server.
- `CANCEL_TRANSFORM` — cancel an active preview without changing the world.

`MOVE_FORWARD`, `MOVE_BACKWARD`, `MOVE_LEFT`, and `MOVE_RIGHT` use the player's horizontal view, rounded to the nearest cardinal direction. `MOVE_UP` and `MOVE_DOWN` always use world axis `Y`.

The first release rotates only around axis `Y`. The pivot is the first box corner or the first individually selected block. A rotation transforms the complete selection as one structure and rotates supported state properties such as `facing` and `axis` with it.

## Validation and preview

- A selection is limited to 512 blocks.
- The pivot may be displaced by at most 16 blocks from its original position.
- Every target position must be empty, belong to the moving selection and be freed by the same atomic operation, or contain a non-source fluid. Source fluids are not valid destinations.
- If any target position conflicts with a block outside the selection, the server rejects the whole transform without changing the world.
- Blocks that require support are validated against the completed destination, not the world before the move.
- The preview renders translucent projected blocks. Valid projected blocks are outlined in blue; conflicting or out-of-range blocks are outlined in red.
- The server is authoritative: it validates selection size, displacement, permissions, destination state, block survival, and rotation support before applying the operation.
- `REPORT_TRANSFORM_FAILURE` reports the rejection reason to the player and leaves the selection in place.

The preview is client-side feedback. The server validation result is the final authority for every transform.

## Explicit non-goals for the first release

- `COPY`, `PASTE`, block deletion, or overwriting destination blocks.
- Rotations around axes `X` and `Z`.
- Unlimited selection size or movement distance.
- An Undo command.
- A compatibility guarantee for every third-party `BlockEntity`.
