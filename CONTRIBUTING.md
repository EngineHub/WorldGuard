# 🛠️ Contributing to Community WorldGuard
<p>Thanks for contributing to Community WorldGuard—the faster, more responsive fork of WorldGuard, built for and by the community. We move quickly, patch boldly, and mythify every fix. To keep things smooth, please follow these guidelines. And if you hit a snag or spot an issue, brag to me, not the WorldGuard team—I’ll help you troubleshoot it.</p>

# 📏 Code Style & Standards
<em>To keep our codebase clean, readable, and mythically maintainable:

Follow the Oracle Java coding conventions. Clean code = fast merges.

Target Java 21 for both source and compilation.

Use spaces only. Indentation must be 4 spaces—no tabs allowed.

Wrap lines at 89 characters. This helps with side-by-side diffs. If wrapping hurts readability, break the rule.

Write full Javadocs for public methods. Fill out @param and @return fields properly—no blanks.

Skip the @author tag. Legacy classes may have it, but we’re phasing it out.

Think through your logic. If your code feels convoluted or repeats large chunks, refactor it. Ten minutes of thought saves hours of debugging.

Keep commit summaries under 70 characters. For details, add two newlines after the summary and write away.

Test your code. Broken code doesn’t help anyone.

Write unit tests for complex logic. Optional, but highly recommended.</em>

## ✅ Submission Checklist
Before you hit submit, run through this:

✅ Tabs replaced with 4-space indents?

✅ Javadocs written for public methods, with filled-out @param and @return?

✅ Pull request rebased to the latest commit of the target branch?

✅ Commits squashed into a small number (ideally one) using git rebase?

✅ Pull request scoped to a manageable size? Large changes should be discussed before you start.

✅ Commit messages are clear and descriptive?

If you’re unfamiliar with git rebase, check out this guide. It lets you rewrite commit messages, combine or split commits, and clean up your history.

🧙 Code Example
# GOOD:

java
if (var.func(param1, param2)) {
    // do things
}
# EXTREMELY BAD:

java
if(var.func( param1, param2 ))
{
    // do things
}
