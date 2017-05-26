# Communicate

## About 

Communicate is a spiritual successor to lopex/multibot and OlegYch/multibot

This family of libraries allows you to
create a bot that enables code interpretation.

Currently each bot implementation is just a 
fork of multibot so if a specific bot fixes a bug, 
It has to be propagated out to the other repos manually.

This is often useful for educational purposes. 
Or finding if a bug is reproducible.

## Goals

- Support multiple languages
- Support multiple interpreters for a single language
  - Different language platforms
  - Different language versions
  - Different library versions
- Support multiple frontends (IRC/Gitter/Slack)
- Be extremely modular

## Planned api support

- Chatrooms
 - Gitter
 - IRC
- Forums and Issue trackers
 - Github

## Planned language support

- Scala
  - JVM
  - JS
  - Native
- Haskell
- Idris

## Navigating the repo

- ./interpreters/$language holds the actual code interpreters
- ./api/$api holds the code to interface with specific api