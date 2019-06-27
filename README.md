# ShapePaint

### Components
Built using Java and Swing components. 

### How to Use
Gradle and Java 1.8 are required.
```
gradle build
```

```
gradle run
```

### Features
#### Tools
- Selection tool: Allows user to move shapes around, and selected tools can have their colors and border/line widths changed.
To deselect the current shape, press ESC or select another shape.
- Eraser tool: Erases any shape the user clicks on.
- Line tool: Draws a line using mouse drag.
- Ellipse tool: Draw an ellipse using mouse drag.
- Rectangle tool: Draw a rectangle using mouse drag.
#### System Level
- Supports saving and loading previous drawings.
- Can copy paste drawings to clipboard and use in other apps (eg. Photoshop)


### Behaviours
When shapes overlap, will select the shape farthest back first.

Selected buttons and shapes are outlined in a cyan border.

### Licenses
Cursor: License: Linkware
http://icons8.com

Eraser: License: Public Domain
https://thenounproject.com/term/eraser/3715/

Line, Rectangle, Ellipse, Paint bucket, Color picker: Creative Commons
Author: Fabiano Coelho, BR
https://thenounproject.com/failureiscool/collection/photoshop-tools/
