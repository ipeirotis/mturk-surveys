Review the current branch's changes against the default branch for a pull request.

Determine the default branch by running `git symbolic-ref refs/remotes/origin/HEAD | sed 's@^refs/remotes/origin/@@'` (typically `master`). Then run `git diff <default-branch>...HEAD` and review all changes for:

1. **Security**: OWASP top 10 issues (injection, XSS, broken auth). Check that no secrets or credentials are committed.
2. **Jakarta namespace**: Flag any `javax.*` imports — this project uses `jakarta.*` (Spring Boot 3.x).
3. **Objectify patterns**: Verify correct use of `@Entity`, `@Index`, `@Cache` annotations. Ensure new queries have matching indexes in `index.yaml`.
4. **Spring Boot conventions**: Proper use of `@RestController`, `@Service`, `@Autowired`. Check error handling via `RestResponseEntityExceptionHandler`.
5. **Frontend**: If Vue/JS files changed, verify CDN-only approach (no npm/node). Check for XSS in template rendering.
6. **Data model**: If entities changed, check backward compatibility with existing Datastore data.

Report findings grouped by severity: CRITICAL, WARNING, INFO.
