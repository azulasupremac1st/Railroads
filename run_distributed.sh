#!/usr/bin/env bash

if [ -z "$MPJ_HOME" ]; then
  echo "MPJ_HOME is not set. "
  echo "Set it to your MPJExpress folder first. "
  exit 1
fi

CORES=$(getconf _NPROCESSORS_ONLN 2>/dev/null)

if [ -z "$CORES" ]; then
  CORES=2
fi


echo "Using $CORES MPI ranks. "
echo "Rank 0 = controller, $((CORES-1)) ranks = workers"

"$MPJ_HOME/bin/mpjrun.sh" -np "$CORES" -cp target/classes org.example.MpiText