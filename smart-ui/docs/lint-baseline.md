# ESLint Baseline (CR-C3-001)

Recorded: 2026-06-12, branch chore/eslint-upgrade-baseline (parent of this commit: `eab1d3c`)

Command: `eslint --ext .js,.vue src` (no fix) with the upgraded config
(eslint:recommended + plugin:vue/recommended, all inherited rules demoted to warn;
no-debugger and no-empty are errors). Vendored code excluded via .eslintignore
(src/util/lrz.all.bundle.js, src/vendor/).

> no-empty is now an error: all 82 legacy empty blocks were triaged and cleaned
> (dead branches removed, conditions inverted where the else carried the logic,
> intentional empty catches annotated with explanatory comments).

## Totals

| Files scanned | Files with issues | Errors | Warnings |
|---|---|---|---|
| 807 | 525 | 0 | 25881 |

> Updated 2026-06-12 (branch fix/axios-interceptor-reject-gated, CR-C14-002/003):
> - All 138 empty Promise `.catch(() => {})` bodies were filled with `console.error`
>   (the report's "26 处" undercounted: 26 was only the `.catch(() => {})` literal;
>   `.catch(err => {})`, multi-line and comment-only variants brought the real total
>   to 138 across 96 files).
> - New `no-restricted-syntax` **error** bans empty `.catch()` callbacks (arrow and
>   function expressions). `no-empty` only covers try/catch blocks, so this selector
>   is what actually prevents regressions for promises.
>   Known boundary: handlers that swallow without an empty block (`.catch(() => undefined)`, `.catch(noop)`) still pass the rule.
> - `no-console` now allows `console.error` / `console.warn` (the sanctioned error
>   visibility channel); plain `console.log` is still a warning.
> - Net warnings 25978 -> 25881 (the allow-list removed legacy console.error/warn
>   warnings; the new console.error fills add none).


> Updated after declaring real runtime globals (tce / axios / CryptoJS) in .eslintrc.js:
> 26252 -> 26167 warnings; no-undef 130 -> 45. The remaining 45 no-undef hits were real
> defects (typos, missing imports, undeclared vars).
>
> Updated again after fixing the 36 unambiguous defects (alter->alert typos, _this
> outside its declaring scope, undeclared msg in error handlers, social_security delObj
> undefined query, missing imports, dead getAreas method): 26167 -> 26131 warnings;
> no-undef 45 -> 9. The last 9 sites referenced backend APIs that
> do not exist in their module; a joint Claude/Codex review proved every one of them
> unreachable (delete/add buttons disabled in crud config, methods with no template
> binding, an orphaned component chain) and they were removed as dead code, taking
> no-undef to 0.

## Per-rule counts

| Rule | Errors | Warnings |
|---|---|---|
| vue/max-attributes-per-line | 0 | 11407 |
| vue/attributes-order | 0 | 3925 |
| vue/html-self-closing | 0 | 3071 |
| vue/mustache-interpolation-spacing | 0 | 2213 |
| id-length | 0 | 1126 |
| no-unused-vars | 0 | 1015 |
| vue/html-indent | 0 | 1007 |
| vue/order-in-components | 0 | 780 |
| vue/require-default-prop | 0 | 296 |
| vue/name-property-casing | 0 | 240 |
| vue/attribute-hyphenation | 0 | 238 |
| no-mixed-spaces-and-tabs | 0 | 165 |
| max-lines | 0 | 137 |
| no-undef | 0 | 0 |
| no-empty | 0 | 0 |
| no-redeclare | 0 | 62 |
| vue/no-multi-spaces | 0 | 49 |
| no-restricted-imports | 0 | 25 |
| no-unreachable | 0 | 24 |
| vue/html-quotes | 0 | 20 |
| no-case-declarations | 0 | 17 |
| no-irregular-whitespace | 0 | 17 |
| complexity | 0 | 12 |
| no-dupe-keys | 0 | 8 |
| no-extra-semi | 0 | 8 |
| vue/require-prop-types | 0 | 7 |
| no-useless-escape | 0 | 4 |
| vue/v-bind-style | 0 | 4 |
| vue/this-in-template | 0 | 4 |
| no-sparse-arrays | 0 | 1 |
| vue/html-end-tags | 0 | 1 |

## Policy

- These counts are the accepted legacy baseline. Do NOT mass-fix existing violations.
- New or modified code must not add to these counts; reviewers should treat new
  warnings in a diff as change requests.
- The pre-commit hook (husky + lint-staged) runs `eslint --fix` and `prettier --write`
  on staged files only, so touched files converge incrementally.
- Notable signals worth follow-up tasks (do not fix here): 24 no-unreachable;
  8 no-dupe-keys; 62 no-redeclare.
