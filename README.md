# iTools SMS Viewer

SMS viewer for backups from iTools Super Backup. Allows loading data from sms.db, viewing chats by numbers, filtering by search (by date or message text).

Simple GUI on Python with Tkinter.

## Preview

<img src="https://rawgit.com/valsaven/itools-sms-viewer/master/preview.avif" alt="preview" width="300">

## Requirements

- Python 3.14+ (Tkinter and sqlite3 are built-in, no need to install anything).
- `sms.db` file from iTools backup (usually in the iPhone backup folder).
- uv (optional, for faster dependency management and running; install via `pip install uv` or from <https://docs.astral.sh/uv/>).

## Installation

Clone the repository:

```sh
git clone https://github.com/valsaven/itools-sms-viewer.git
cd itools-sms-viewer
```

## Launch

Place `sms.db` in the folder with `app.py` and run:

```sh
python app.py
```

Alternatively, if you have `uv` installed (recommended for better performance):

```sh
uv run app.py
```

## Build to EXE (for Windows)

Install PyInstaller if not already:

```sh
pip install pyinstaller
```

Or with uv:

```sh
uv pip install pyinstaller
```

Build:

```sh
pyinstaller --onefile app.py
```

The EXE will be in the dist folder. Copy `sms.db` there for it to work.

## Tested on

- Apple iPhone 4 A1332 32 GB (GSM)

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
