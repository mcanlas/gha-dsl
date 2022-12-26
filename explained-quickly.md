# GitHub Actions explained quickly

- All workflow files under `.github/workflows` have the potential to be triggered at any time, for the triggers they opt-in to
- Only by convention is there a workflow that runs per pull request or merge to main
- Each workflow consists of one to many jobs
- Each job, by default, runs in parallel
- Jobs can be sequenced by the downstream job depending on the upstream one
- One job is a sequence of one to many steps
