# dfx-mosaic

## Abstract

This is a toy project created after [JavaLand 2015][1]. Wolf Nkole Helzle had this [My-Matrix][2] project going on there and i thought, well, this year i'm doing [Daily Fratze][3] for ten years and i alone will have 3650 pictures of myself uploaded to the internet by August and in sum there are over 50.000 images right now, that should be a pretty good idea, creating an interactive mosaic as well ([Adastra][4] has already created a beautiful [mosaic on her 8th anniverary][5]).

As i don't want to share the images anywhere else but only in my own daily picture project i had the opportunity to learn some more stuff which you can find here.

The basic idea for this mosaic generator is: Create an accurate color matching algorithm as readible as possible. [Lukas Eder][7] had this nice article on dzone [why It's Okay to Stick With SQL][8] and i wanted to see how far can i get with SQL for my goal.

This project includes:

* [Flyway][6] for creating and migrating databases (i didn't want to run sql-scripts myself)
* Flyway and [jOOQ][9] maven integration for effortless generating dao code at compile time
* jOOQ in common for all database access
* An CIE94 color distance algorithm
* JavaFX 3d for creating a spherical image wall.

Have a look at my blog post [JavaLand 2015 result: JavaFX 3d mosaic for dailyfratze.de][10] for an introduction and some more details.

[1]: http://www.javaland.eu
[2]: http://www.my-matrix.org
[3]: https://dailyfratze.de/michael
[4]: https://dailyfratze.de/adastra
[5]: http://adastra.me/2014/11/ive-been-taking-selfies-since-before-it-was-uncool/
[6]: http://flywaydb.org
[7]: https://twitter.com/lukaseder
[8]: http://java.dzone.com/articles/3-reasons-why-its-okay-stick
[9]: http://www.jooq.org
[10]: http://info.michael-simons.eu/2015/03/31/javaland-2015-result-javafx-3d-mosaic-for-dailyfratze-de/