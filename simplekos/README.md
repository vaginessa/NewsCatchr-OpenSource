# Simple KeyObjectStore (KOS)

This library is aimed to provide an easy solution to save objects to the storage. Each store is a directory with files for each key inside. A key is the reference to a specific saved item.

The library uses [Ason](https://github.com/afollestad/ason) to serialize and deserialize objects. Please check the documentation to learn how to annotate classes for serialization.

It is developed in Kotlin. Java interoperability isn't tested.

## LICENSE

```
NewsCatchr
Copyright © 2017 Jan-Lukas Else

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
