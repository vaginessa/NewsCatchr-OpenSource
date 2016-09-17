# NewsCatchr

[Website](https://newscatchr.jlelse.eu)  
[Download](https://play.google.com/store/apps/details?id=jlelse.readit)

## Why Open Source now?

You know, I'm the laziest developer on earth and I'm pissed about the app now, so do whatever you want!
No, that was just a JOKE!

I believe in OPEN SOURCE, I believe, that apps will get better when they are open source and there's a community, which likes to contribute. And because I don't need yet, I thought "What could go wrong? - Nothing.".

## How can YOU contribute?

Things you have to know:

- The app is mostly written in Kotlin (but it's very similar to Java)
- When you want to build the app, you should use Android Studio or IntelliJ IDEA with the Kotlin plugin installed
- The API keys, used buy the Play Store version are removed from this code, enter your own under `mobile/src/main/java/jlelse/newscatchr/backend/apis/ApiKeys.kt`

Some things you can help with:

- Translate the app into your language [here](http://translate.jlelse.eu)
- Fix bugs, described on the [issue reporter](https://github.com/jlelse/NewsCatchr-OpenSource/issues)
- Report bugs on the [issue reporter](https://github.com/jlelse/NewsCatchr-OpenSource/issues)
- And everything else, that comes into your mind

## What has changed since the last Play Store release?

- Read state indicator (_italic title_) including backup / restore of it
- Big changes to the way, the app gets the articles from feedly
- Restructured some settings
- Sync feature (download new articles on a specific time interval)
- Google Voice Search implementation
- Direct posting to Facebook and Twitter
- Url Shortener (very useful when sharing to Twitter)
- Big UI changes (FABs, new colors, new icon etc.)
- Improved Readability feature (reparsing articles), it uses the WordPress API now too
- Totally reworked home screen with last opened feeds, favorite feeds and suggestions based on your language
- Links in the article text will open with Custom Tabs too (if configured)
- Added possibility to directly submit issues from within the app
- Improved API integrations
- Added German translation and added option to change the language manually
- Updated many dependencies
- And more

You can test the beta version with all the new stuff via [this link](https://play.google.com/apps/testing/jlelse.readit). But it might be *UNSTABLE*!

## LICENSE

```
NewsCatchr  Copyright © 2016  Jan-Lukas Else

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
```