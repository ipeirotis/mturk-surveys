Deploy the app to Google App Engine.

Steps:
1. Build with `JAVA_TOOL_OPTIONS="" mvn clean install -Dmaven.resolver.transport=wagon -B`
2. Deploy the app: `mvn appengine:deploy -B -Dapp.deploy.promote=true`
3. Deploy cron jobs: `gcloud app deploy src/main/appengine/cron.yaml --project=mturk-demographics --quiet`
4. Deploy Datastore indexes: `gcloud app deploy src/main/appengine/index.yaml --project=mturk-demographics --quiet`
5. Verify the deployment by checking `https://demographics.mturk-tracker.com/actuator/health`

If any step fails, stop and report the error. Do not proceed to the next step.
