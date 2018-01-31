

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that generates and allows programmatic exploration of a maze.
 */
public class Maze {

    /**
     * Exception for bad direction inputs.
     */
    public static final class DirectionException extends Exception {

        /**
         * Create a new direction exception.
         *
         * @param message the message
         */
        DirectionException(final String message) {
            super(message);
        }
    }

    /**
     * Exception for bad locations.
     */
    public static final class LocationException extends Exception {

        /**
         * Create a new location exception.
         *
         * @param message the message
         */
        LocationException(final String message) {
            super(message);
        }
    }

    /**
     * Class representing a 2D integer location.
     */
    public static class Location {

        /**
         * My X and Y coordinates.
         */
        private int x, y;

        /**
         * Get my X coordinate.
         *
         * @return my X coordinate
         */
        public int x() {
            return x;
        }

        /**
         * Sets my X coordinate.
         *
         * @param setX my new X coordinate
         */
        public void setX(final int setX) {
            this.x = setX;
        }

        /**
         * Get my Y coordinate.
         *
         * @return my Y coordinate
         */
        public int y() {
            return y;
        }

        /**
         * Sets my Y coordinate.
         *
         * @param setY my new Y coordinate
         */
        public void setY(final int setY) {
            this.y = setY;
        }

        /**
         * Create a new location.
         *
         * @param setX the X coordinate
         * @param setY the Y coordinate
         */
        public Location(final int setX, final int setY) {
            x = setX;
            y = setY;
        }

        @Override
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Location other = (Location) obj;
            if (x != other.x) {
                return false;
            }
            if (y != other.y) {
                return false;
            }
            return true;
        }

        /**
         * Combine two locations.
         *
         * @param other the other
         * @return the location
         */
        public Location add(final Location other) {
            return new Location(x + other.x, y + other.y);
        }

        @Override
        public final String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    /**
     * Possible movement directions.
     */
    public static final String[] DIRECTIONS = {new String("up"), new String("right"),
            new String("down"), new String("left")};

    /**
     * Class representing a possible movement.
     */
    static class Movement {

        /**
         * The change in the X value.
         */
        private Location d;

        /**
         * Get the change in location.
         *
         * @return the change in the X value
         */
        public Location getD() {
            return d;
        }

        /**
         * Gets the movement direction.
         *
         * @return the movement direction
         */
        public String getDirection() {
            return direction;
        }

        /**
         * Sets the movement direction.
         *
         * @param newDirection the new movement direction
         */
        public void setDirection(final String newDirection) {
            this.direction = newDirection;
        }

        /**
         * The direction.
         */
        private String direction;

        /**
         * Instantiates a new movement.
         *
         * @param setD         the change in location
         * @param setDirection the set direction
         * @throws DirectionException exception if a bad direction is provided
         */
        Movement(final Location setD, final String setDirection) throws DirectionException {
            d = setD;
            if (!(Arrays.asList(DIRECTIONS).contains(setDirection))) {
                throw new DirectionException(setDirection + " is not a valid direction");
            }
            direction = setDirection;
        }
    }

    /**
     * Map of possible movements.
     */
    static final Map<String, Movement> MOVEMENTS;

    /**
     * Convenience map of opposite directions.
     */
    static final Map<String, String> OPPOSITEDIRECTIONS;

    static {
        MOVEMENTS = new HashMap<String, Movement>();
        try {
            MOVEMENTS.put("up", new Movement(new Location(0, 1), "up"));
            MOVEMENTS.put("down", new Movement(new Location(0, -1), "down"));
            MOVEMENTS.put("right", new Movement(new Location(1, 0), "right"));
            MOVEMENTS.put("left", new Movement(new Location(-1, 0), "left"));
        } catch (DirectionException e) {
            e.printStackTrace();
        }

        OPPOSITEDIRECTIONS = new HashMap<String, String>();
        OPPOSITEDIRECTIONS.put("up", "down");
        OPPOSITEDIRECTIONS.put("down", "up");
        OPPOSITEDIRECTIONS.put("left", "right");
        OPPOSITEDIRECTIONS.put("right", "left");
    }

    /**
     * A class representing a cell in the maze.
     */
    class Cell {

        /**
         * The cell's location.
         */
        private final Location location;

        /**
         * Gets cell's location.
         *
         * @return the location
         */
        public Location getLocation() {
            return location;
        }

        /**
         * Whether we have visited the cell during maze construction.
         */
        private boolean visited;

        /**
         * Checks if the cell has been visited during maze creation.
         *
         * @return true, if it has been visited
         */
        public boolean isVisited() {
            return visited;
        }

        /**
         * Mark the cell as visited.
         */
        public void setAsVisited() {
            this.visited = true;
        }

        /**
         * Map of cell up, right, down, left borders.
         */
        private Map<String, Boolean> borders;

        /**
         * Gets the borders.
         *
         * @param direction the direction to set
         * @return the borders
         */
        public Boolean getBorder(final String direction) {
            return borders.get(direction);
        }

        /**
         * Clear a borders.
         *
         * @param direction the name of the border to clear
         */
        public void clearBorder(final String direction) {
            this.borders.put(direction, false);
        }

        /**
         * Map of cell up, right, down, left neighboring cells, if they exist.
         */
        private Map<String, Cell> neighbors;

        /**
         * Get a cell's neighbors.
         *
         * @return the neighbors of the cell
         */
        public Map<String, Cell> getNeighbors() {
            return neighbors;
        }

        /**
         * Set a given neighbors.
         *
         * @param direction the direction to set
         * @param neighbor  the cell to set as a neighbor
         */
        public void setNeighbor(final String direction, final Cell neighbor) {
            this.neighbors.put(direction, neighbor);
        }

        /**
         * Relative direction using during maze construction.
         */
        private String relativeDirection;

        /**
         * Gets the relative direction. Used during creation.
         *
         * @return the relative direction
         */
        public String getRelativeDirection() {
            return relativeDirection;
        }

        /**
         * Sets the relative direction. Used during creation.
         *
         * @param setRelativeDirection the new relative direction
         */
        public void setRelativeDirection(final String setRelativeDirection) {
            this.relativeDirection = setRelativeDirection;
        }

        /**
         * Create a new cell.
         *
         * @param setLocation the cell's location
         */
        Cell(final Location setLocation) {
            location = setLocation;
            borders = new HashMap<String, Boolean>();
            for (String direction : DIRECTIONS) {
                borders.put(direction, true);
            }
            neighbors = new HashMap<String, Cell>();
        }
    }

    /**
     * The maze.
     */
    private Cell[][] maze;

    /**
     * Create a new maze object with the specified dimensions.
     *
     * @param location the location to check
     * @return true, if successful
     */
    private boolean validLocation(final Location location) {
        return (location.x() >= 0 && location.x() < myXDimension && location.y() >= 0
                && location.y() < myYDimension);
    }

    /**
     * The x dimension.
     */
    private final int myXDimension;

    /**
     * Gets the maze X dimension.
     *
     * @return the maze's X dimension
     */
    public int getxDimension() {
        return myXDimension;
    }

    /**
     * The y dimension.
     */
    private final int myYDimension;

    /**
     * Gets the maze Y dimension.
     *
     * @return the maze's Y dimension
     */
    public int getyDimension() {
        return myYDimension;
    }

    /**
     * Create a new randomly-generated maze.
     *
     * @param mazeXDimension the x dimension
     * @param mazeYDimension the y dimension
     */
    public Maze(final int mazeXDimension, final int mazeYDimension) {

        myXDimension = mazeXDimension;
        myYDimension = mazeYDimension;

        if (myXDimension < 1) {
            throw new IllegalArgumentException("xDimension too small");
        }
        if (myYDimension < 1) {
            throw new IllegalArgumentException("yDimension too small");
        }
        if (myXDimension * myYDimension <= 1) {
            throw new IllegalArgumentException("combined dimensions too small");
        }

        maze = new Cell[myXDimension][myYDimension];
        for (int x = 0; x < myXDimension; x++) {
            for (int y = 0; y < myYDimension; y++) {
                maze[x][y] = new Cell(new Location(x, y));
            }
        }
        for (int x = 0; x < myXDimension; x++) {
            for (int y = 0; y < myYDimension; y++) {
                for (Map.Entry<String, Movement> entry : MOVEMENTS.entrySet()) {
                    Movement movement = entry.getValue();
                    Location neighborLocation = maze[x][y].getLocation().add(movement.getD());
                    if (!(validLocation(neighborLocation))) {
                        continue;
                    }
                    maze[x][y].setNeighbor(movement.direction,
                            maze[neighborLocation.x()][neighborLocation.y()]);
                }
            }
        }

        int randomX = ThreadLocalRandom.current().nextInt(0, myXDimension);
        int randomY = ThreadLocalRandom.current().nextInt(0, myYDimension);
        Cell currentCell = maze[randomX][randomY];
        Stack<Cell> path = new Stack<Cell>();
        path.push(currentCell);
        int unvisitedCount = myXDimension * myYDimension - 1;

        while (unvisitedCount > 0) {
            if (path.empty()) {
                throw new IllegalStateException("path should not be empty");
            }

            currentCell = path.peek();
            currentCell.setAsVisited();

            ArrayList<Cell> nextCells = new ArrayList<Cell>();
            for (Map.Entry<String, Cell> entry : currentCell.getNeighbors().entrySet()) {
                Cell neighbor = entry.getValue();
                if (!(neighbor.isVisited())) {
                    neighbor.setRelativeDirection(entry.getKey());
                    nextCells.add(neighbor);
                }
            }

            if (nextCells.size() == 0) {
                path.pop();
                continue;
            }

            Cell nextCell = nextCells.get(ThreadLocalRandom.current().nextInt(0, nextCells.size()));
            if (currentCell.getBorder(nextCell.relativeDirection) == false) {
                throw new IllegalStateException("current cell should not have this border");
            }
            currentCell.clearBorder(nextCell.relativeDirection);
            String oppositeDirection = OPPOSITEDIRECTIONS.get(nextCell.relativeDirection);
            if (nextCell.getBorder(oppositeDirection) == false) {
                throw new IllegalStateException("next cell should not have this border");
            }
            nextCell.clearBorder(oppositeDirection);

            unvisitedCount--;
            path.add(nextCell);
        }
        for (int x = 0; x < myXDimension; x++) {
            for (int y = 0; y < myYDimension; y++) {
                Cell cell = maze[x][y];
                if (y == myYDimension - 1 && cell.getBorder("up") == false) {
                    throw new IllegalStateException("top row should have this border");
                }
                if (y == 0 && cell.getBorder("down") == false) {
                    throw new IllegalStateException("bottom row should have this border");
                }
                if (x == myXDimension - 1 && cell.getBorder("right") == false) {
                    throw new IllegalStateException("right column should have this border");
                }
                if (x == 0 && cell.getBorder("left") == false) {
                    throw new IllegalStateException("left column should have this border");
                }
                for (Map.Entry<String, Cell> entry : cell.neighbors.entrySet()) {
                    Cell neighbor = entry.getValue();
                    String direction = entry.getKey();
                    String oppositeDirection = OPPOSITEDIRECTIONS.get(direction);
                    if (cell.getBorder(direction) != neighbor.getBorder(oppositeDirection)) {
                        throw new IllegalStateException("mismatched neigbor borders");
                    }
                }
            }
        }
    }

    /**
     * The user's current X, Y location.
     */
    private Location currentLocation = null;

    /**
     * Start the maze at a specific location.
     *
     * @param x the starting X coordinate
     * @param y the starting Y coordinate
     * @throws LocationException a location exception the location is invalid
     */
    public void startAt(final int x, final int y) throws LocationException {
        Location potentialLocation = new Location(x, y);
        if (!validLocation(potentialLocation)) {
            throw new LocationException("can't set maze end at invalid location");
        }
        currentLocation = potentialLocation;
    }

    /**
     * Start the maze at (0, 0).
     */
    public void startAtZero() {
        currentLocation = new Location(0, 0);
    }

    /**
     * Start the maze at a random location.
     */
    public void startAtRandomLocation() {
        Location newLocation;
        do {
            newLocation = new Location(ThreadLocalRandom.current().nextInt(0, myXDimension),
                    ThreadLocalRandom.current().nextInt(0, myYDimension));
        } while (currentLocation == newLocation);
        currentLocation = newLocation;
    }

    /**
     * Get the current location.
     *
     * @return the current location
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * The maze's end location.
     */
    private Location endLocation = null;

    /**
     * End the maze at a specific location.
     *
     * @param x the starting X coordinate
     * @param y the starting Y coordinate
     * @throws LocationException a location exception the location is invalid
     */
    public void endAt(final int x, final int y) throws LocationException {
        Location potentialLocation = new Location(x, y);
        if (!validLocation(potentialLocation)) {
            throw new LocationException("can't set maze end at invalid location");
        }
        endLocation = new Location(x, y);
    }

    /**
     * End the maze at the top right corner.
     */
    public void endAtTopRight() {
        endLocation = new Location(myXDimension - 1, myYDimension - 1);
    }

    /**
     * End the maze at a random location.
     */
    public void endAtRandomLocation() {
        Location newLocation;
        do {
            newLocation = new Location(ThreadLocalRandom.current().nextInt(0, myXDimension),
                    ThreadLocalRandom.current().nextInt(0, myYDimension));
        } while (endLocation == newLocation);
        endLocation = newLocation;
    }

    /**
     * Get the end location.
     *
     * @return the current location
     */
    public Location getEndLocation() {
        return endLocation;
    }

    /**
     * The current movement direction.
     */
    private String currentDirection = "up";

    /**
     * Attempt to move forward in the given direction. Returns true if the move succeeded, and false
     * if a wall is in the way.
     *
     * @return true if the move completed, false if it did not
     */
    public boolean move() {
        Cell currentCell = maze[currentLocation.x()][currentLocation.y()];
        if (currentCell.getBorder(currentDirection)) {
            return false;
        } else {
            currentLocation = currentLocation.add(MOVEMENTS.get(currentDirection).getD());
            return true;
        }
    }

    /**
     * Return if you can move in the current direction.
     *
     * @return true if you can, false if a wall is in the way
     */
    public boolean canMove() {
        Cell currentCell = maze[currentLocation.x()][currentLocation.y()];
        return !currentCell.getBorder(currentDirection);
    }

    /**
     * Turn left.
     */
    @SuppressWarnings("checkstyle:missingswitchdefault")
    public void turnLeft() {
        switch (currentDirection) {
            case "up":
                currentDirection = "left";
                break;
            case "left":
                currentDirection = "down";
                break;
            case "down":
                currentDirection = "right";
                break;
            case "right":
                currentDirection = "up";
                break;
        }
    }

    /**
     * Turn right.
     */
    @SuppressWarnings("checkstyle:missingswitchdefault")
    public void turnRight() {
        switch (currentDirection) {
            case "up":
                currentDirection = "right";
                break;
            case "right":
                currentDirection = "down";
                break;
            case "down":
                currentDirection = "left";
                break;
            case "left":
                currentDirection = "up";
                break;
        }
    }

    /**
     * Return true when you have completed the maze.
     *
     * @return true if you are at the maze end point, false otherwise
     */
    public boolean isFinished() {
        return currentLocation.equals(endLocation);
    }

    @Override
    @SuppressWarnings("checkstyle:innerassignment")
    public final String toString() {
        String returnString = "";
        char[][] cellDrawing = new char[2 * myXDimension + 1][2 * myYDimension + 1];

        for (int y = 0; y < 2 * myYDimension; y++) {
            for (int x = 0; x < 2 * myXDimension; x++) {
                cellDrawing[x][y] = ' ';
            }
        }
        for (int x = 0; x < myXDimension; x++) {
            for (int y = 0; y < myYDimension; y++) {
                int xOffset = 2 * x;
                int yOffset = 2 * y;
                if (maze[x][y].getBorder("up")) {
                    cellDrawing[xOffset][yOffset + 2] =
                            cellDrawing[xOffset + 1][yOffset + 2] =
                                    cellDrawing[xOffset + 2][yOffset + 2] = '#';
                }
                if (maze[x][y].getBorder("right")) {
                    cellDrawing[xOffset + 2][yOffset] =
                            cellDrawing[xOffset + 2][yOffset + 1] =
                                    cellDrawing[xOffset + 2][yOffset + 2] = '#';
                }
                if (maze[x][y].getBorder("down")) {
                    cellDrawing[xOffset][yOffset] =
                            cellDrawing[xOffset + 1][yOffset] =
                                    cellDrawing[xOffset + 2][yOffset] = '#';
                }
                if (maze[x][y].getBorder("left")) {
                    cellDrawing[xOffset][yOffset] =
                            cellDrawing[xOffset][yOffset + 1] =
                                    cellDrawing[xOffset][yOffset + 2] = '#';
                }
                if (x == currentLocation.x() && y == currentLocation.y()) {
                    cellDrawing[xOffset + 1][yOffset + 1] = 'X';
                } else if (x == endLocation.x() && y == endLocation.y()) {
                    cellDrawing[xOffset + 1][yOffset + 1] = 'E';
                }
            }
        }
        for (int x = 0; x < 2 * myXDimension + 1; x++) {
            returnString += "#";
        }
        returnString += "\n";
        for (int y = (2 * myYDimension) - 1; y >= 0; y--) {
            for (int x = 0; x < 2 * myXDimension; x++) {
                returnString += cellDrawing[x][y];
            }
            returnString += "#\n";
        }
        return returnString;
    }


    /**
     * Solve a randomly-generated maze.
     *
     * @see <a href="https://github.com/cs125-illinois/mazemaker">Mazemaker on GitHub</a>
     * @see <a href="https://cs125-illinois.github.io/mazemaker/">Mazemaker Documentation</a>
     * @see <a href="https://cs125.cs.illinois.edu/lab/2/#maze">Lab 2 Writeup</a>
     */
    @SuppressWarnings("checkstyle:emptyblock")

    public class SolveMaze {

        /**
         * Implement your maze solving algorithm in the main method below.
         *
         * @param unused unused input arguments
         */

        public void main(final String[] unused) {
            /*
             * Create a new 10 x 10 maze. Feel free to change these values.
             */
            Maze maze = new Maze(10, 10);

            /*
             * Pick (0, 0), the bottom left corner, as the starting point.
             * Put the end in the top right corner.
             */
            maze.startAtZero();
            maze.endAtTopRight();

            /*
             * You should be able to solve a 10 x 10 maze in (far fewer than) 1000 steps.
             * Feel free to adjust this number if you experiment with other mazes.
             * canMove(): returns true if you can move forward
             */
            for (int step = 0; step < 1000; step++) {

                while (maze.isFinished() ==false){
                    if (canMove() == true) {
                        move();
                    } else {
                        turnRight();
                        if (canMove() == false) {
                            turnLeft();
                            turnLeft();
                            if (canMove() == false) {
                                turnLeft();
                            }
                        }
                    }
                }

                if (maze.isFinished()) {
                    System.out.println("You solved the maze!");
                } else {
                    System.out.println("Try again!");
                }
            }
        }
    }
}



