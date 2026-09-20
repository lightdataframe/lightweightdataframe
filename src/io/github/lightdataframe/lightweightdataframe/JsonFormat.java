package io.github.lightdataframe.lightweightdataframe;



/**
 * Enum representing different formats for representing {@link Dataframe} objects as JSON strings.
 * This enum defines the following formats:
 * <p>
 * {@code ROWS}: Represents a dataframe organized in rows format.<br>
 * With a M x N dataframe:<br>
 * [<br>
 * </t>{col0 : value00, ..., coln : value0N},<br>
 * ...<br>
 * {colm : valueM0, ..., coln : valueMN},<br>
 * ]<br>
 * <p>
 * {@code COLUMNS}: Represents a dataframe organized in columns format.<br>
 * With a M x N dataframe:<br>
 * {<br>
 * col0 : [value00, ..., value0N],<br>
 * ...<br>
 * colM : [valueM0, ..., valueMN],<br>
 * }<br>
 * <p>
 * <p>
 * {@code COLUMNS_ROWS}: Represents a dataframe organized in columns-rows format.<br>
 * With a M x N dataframe:<br>
 * {<br>
 * columns : [col0, ..., colM],<br>
 * rows : <br>
 * [<br>
 * </t>{col0 : value00, ..., coln : value0N},<br>
 * ...<br>
 * {colm : valueM0, ..., coln : valueMN},<br>
 * ]<br>
 * }
 * <p>
 */
public enum JsonFormat
{
    /**
     * Represents a dataframe with the rows format.
     */
    ROWS,
    /**
     * Represents a dataframe with the columns format.
     */
    COLUMNS,
    /**
     * Represents a dataframe with the columns-rows format.
     */
    COLUMNS_ROWS
}
