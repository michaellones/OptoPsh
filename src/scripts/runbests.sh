# Args: directory
# Find last fitness info in all output files

find $1 -name "output*" -exec bash -c 'for f; do echo -n "$f "; cat "$f" | grep "Best Program Fitness"| tail -1 | rev | cut -d" " -f1 | rev; done' _ {} +
