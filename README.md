# gha-dsl

See also [GitHub Actions explained quickly](explained-quickly.md)

## Goals

- Easily support the generation of multiple GHA workflows
- Building blocks to ease code sharing
- Clear documentation and guideposts for how certain mechanisms work and when they are required
- An "opt-out by default" interface (i.e. the opposite of an overly opinionated framework)

## Non-goals

- Fully type-safe YAML integration
  - The generated YAML should be valid for most cases but does not internally use a YAML model 
- Being an "overly opinionated framework"
