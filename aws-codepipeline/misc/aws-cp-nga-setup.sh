# ask the user
lambdabucket=nga-bucket-oregon

lambdafile=hpngaawscodepipeline-lambda-stable.jar
lambda_execution_role_name="nga_lambda_executor_role"
lambda_execution_access_policy_name="NGA-CodePipeline-Integration-policy"
lambdascheduledname="NgaScheduledFunction"
lambdareportname="NgaReportFunction"

echo "Uploading NGA configuration custom build action type"
aws codepipeline create-custom-action-type --cli-input-json file://build-action.json
echo "Creating lambda execution role $lambda_execution_role_name"
lambda_execution_role_arn=$(aws iam create-role \
  --role-name "$lambda_execution_role_name" \
  --assume-role-policy-document '{
      "Version": "2012-10-17",
      "Statement": [
        {
          "Sid": "",
          "Effect": "Allow",
          "Principal": {
            "Service": "lambda.amazonaws.com"
          },
          "Action": "sts:AssumeRole"
        }
      ]
    }' \
  --output text \
  --query 'Role.Arn'
)

echo lambda_execution_role_arn=$lambda_execution_role_arn

echo "Attaching required policy $lambda_execution_access_policy_name to lambda execution role $lambda_execution_role_name"
aws iam put-role-policy \
  --role-name "$lambda_execution_role_name" \
  --policy-name "$lambda_execution_access_policy_name" \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        "Resource": "arn:aws:logs:*:*:*"
      },
      {
        "Effect": "Allow",
        "Action": "codepipeline:*",
        "Resource": "*"
      },
      {
        "Effect": "Allow",
        "Action": [
          "s3:GetObject",
          "s3:PutObject"
        ],
        "Resource": "*"
      }
    ]
  }'

echo "Uploading lambda function code to Amazon S3"
aws s3 cp $lambdafile s3://$lambdabucket/$lambdafile

echo "Creating scheduled lambda function"
lambda_function_arn=$(aws lambda create-function --function-name "$lambdascheduledname" --runtime java8 --role $lambda_execution_role_arn --handler "ngalambda.NgaScheduled::handleRequest" --code "S3Bucket=$lambdabucket,S3Key=$lambdafile" --description "NGA CodePipeline scheduled processor" --timeout 60 --memory-size 192 --no-publish --output text --query "FunctionArn")

echo "Adding permission for scheduled lambda execution"
aws lambda add-permission \
    --statement-id 'Allow-scheduled-events' \
    --action 'lambda:InvokeFunction'\
    --principal 'events.amazonaws.com'\
    --function-name $lambda_function_arn

echo "Creating scheduled execution rule"
aws events put-rule \
    --name ScheduledNgaExecutionRule \
    --schedule-expression 'rate(2 minutes)' \
    --description "Periodically check for processable jobs to be pushed to NGA"

echo "Applying scheduled execution rule to lambda function"
aws events put-targets \
    --rule ScheduledNgaExecutionRule \
    --targets "{\"Id\" : \"1\", \"Arn\": \"$lambda_function_arn\"}"

echo "Creating report lambda function"
aws lambda create-function --function-name "$lambdareportname" --runtime java8 --role $lambda_execution_role_arn --handler "ngalambda.NgaReport::handleRequest" --code "S3Bucket=$lambdabucket,S3Key=$lambdafile" --description "NGA CodePipeline test result reporting job" --timeout 60 --memory-size 192 --no-publish
